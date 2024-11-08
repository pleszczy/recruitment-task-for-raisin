# Recruitment task for Raisin

## How to run the program

- install java 21 using brew/sdkman/chocolatey/your favorite linux package manager
- run './gradlew run'

## How to run the program on GraalVM (untested)
- install graalvm- 21
- generate native executable './gradlew nativeCompile'
- executes the generated native executable './gradlew nativeRun'

## Summary

- Records from Source A that have a corresponding record from Source B are "joined" records.
- Records from either Sources that don't have a match from the other Source are "orphaned" records.
- Records that are malformed are "defective" records
- "defective" records can be ignored
- Each Source can produce 0..1 records
- "joined" and "orphaned" records have to be reported to /sink/a
- The ordering in which records are submitted is not important

## Functional requirements

- Records from Source A that have a corresponding record from Source B should be reported as "joined" to the sink.
- Records from either Sources that don't have a match from the other Source should be reported as "orphaned" records to
  the sink.

## Non-functional requirements

- should be able to handle a larger data set
- should not block for any endpoints / handle 406 response (The endpoints are interlinked and might lock until data has been read from or written to)
- should handle late or out-of-order events
- should be able to run indefinitely (e.g. use a windowing approach instead of caching everything until system runs out of memeory)

## Observations/Thoughts

- This scenario appears to be a strong use case for Apache Flink with time windowing or Kafka Streams with the join() (if the data sources were Kafka topics instead of REST endpoints).
- Could use a simple key-value store like RocksDB to implement a time windowing while keeping memory usage low
- **Can brute force through interlocking without calling any other endpoints**
- Can get away with the suboptimal orphaned case handling up to 100k records
  - **100k: Success**
    - "orphans_created": 9745,
    - "orphans_received": 9745,
  - **200k: The solution is falling apart at this point**
    - "orphans_created": 19850,
    - "orphans_received": 14378,


- Observing a small difference between 'joined_created' and 'joined_received'. I've made a mistake somewhere, 
  but it doesn't seem to be correlated with the records total numbers. It's usually off by just 0-2 
  e.g. for 100k records
     "joined_created": 84317,
     "joined_received": 84319,
  Assuming we are getting "at least once" delivery guarantee with some duplicates due to retries

## Simplification / Assumptions
- Orphans make up approximately 10% of the total records.
- Going to handle orphans after done signal due to the time limitations for solving the task. 
  Assuming I won't run out of memory / manage to post all orphans before 15 secs is up / won't get blocked by the dreaded 406.
  This is a borderline non-optimal approach. Should rather use a time window and assume that a record is orphaned if we cant
  find a pair within some fixed time window e.g. 30 secs or after done signal (whichever happens first).
- Ignoring status, assuming its always 'ok' until the 'done' signal.

## Task description

First notes about your program:

It has to produce the right results.
Take your time, there is no hard timebox for this challenge, but normally it is developed around four to eight hours. We
don't consider the time you take to polish your submission's documentation to be part of the challenge's time limit.
You can use the programming language of your choice to solve the challenge, as long you provide instructions on how to
execute it, nevertheless using a language related to the position your are applying for, is a good idea.
The code doesn't have to be perfect, but you need to be able to explain how things could be improved. (e.g., you could
note that, "I'm not checking the return value here")
We want you to make your own choices and we expect you to be able to explain any choices or assumptions you made.
You can make those explanations in the comments.
If you have any questions, feel free to ask.

Problem Description

We have supplied you with a small web server called fixture. It is written in Python, we offer two versions:

fixture_2 will run with Python 2.6 or Python 2.7, while
fixture_3 will run with Python 3.6 or newer.
Pretty much any Unix based system will work (e.g. Linux or a Mac.) You can probably even use a Windows if you want, but
the verification tool may not work.

By default the web server listens on port 7299.

The web server has three endpoints:

/source/a
emits JSON records.
/source/b
emits XML records.
/sink/a
accepts JSON records.
Most records from Source A will have a corresponding record from Source B, these are "joined" records.

Some records from one source will not have a match from the other source, these are "orphaned" records.

Some records are malformed, these are "defective" records.

Each source will emit each record ID either 0 or 1 times.

Your program must read all the records from /source/a and /source/b, categorize them as "joined", "orphaned", or "
defective".

It must report the "joined' and "orphaned" records to /sink/a, It can ignore defective records, the ordering in which
records are submitted is not important.

By default the test program will emit around 1000 records, once all the records have been read from an endpoint it
responds with a "done" message.

You must start sending data before the endpoints are done.

In testing we will run your program against a much larger data set, so your program must behave as if it is going to run
forever.

Here's the catch: Both sources and the sink endpoints are interlinked. Sometimes an endpoint will block until data has
been read from or written to the other endpoints, when this happens the request will return a 406 response. The program
will never deadlock.

Testing

The web server writes your responses and the expected response into its running directory, we supply a program to
compare the these two files.

When we receive your program, we will run it against a much larger data set, you should test your program with a larger
data set too.

Refer to the How To Use The Tools section for more details.

Message Specifications

Endpoint /source/a

normal record:

{ "status": "ok", "id": "XXXXX" }
done record:

{"status": "done"}
Endpoint /source/b

normal record:

<?xml version="1.0" encoding="UTF-8"?><msg><id value="$ID"/></msg>
done record:

<?xml version="1.0" encoding="UTF-8"?><msg><done/></msg>
Endpoint /sink/a

To endpoint in POST body:

{"kind": "$KIND", "id": "$ID"},
where

$KIND can be either "joined" or "orphaned", and
$ID is the id from the originating messages.
Success response:

{"status": "ok"}
Failure response:

{"status": "fail"}
How To Use The Tools

Execute the web server. This mostly depends on your environment and the version you want to run.

In most cases, you'll run the server with

cd be-challenge/
./fixture_3.py
If you're using MacOS Mojave or older and don't have a custom python3 install, you should run the version for python2

cd be-challenge/
./fixture_2.py
If previous commands failed, read the documentation of the python version installed in your system.

By default the fixture emits 1000 records. You can choose the number of records with -n COUNT option. E.g.
./fixture_3.py -n 50000

The fixture server terminates fifteen (15) seconds after both sources are done.

When the fixture terminates it will print a set of counters. These values may be useful to you.

The output will appear in the files submitted.txt and expected.txt in the fixture's execution directory.

Compare the submitted.txt and expected.txt files by executing:

sh check.sh
If all the records match, the comparison program will terminate with the message good and exit code 0.

If there are differences between the expected and submitted records then it prints a diff.

The expected records appear after the lines starting with <.
Your records appear after the lines starting with >.
After printing the diff, it prints bad and terminates with exit code 1.

Good luck!
