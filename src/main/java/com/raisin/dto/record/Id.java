package com.raisin.dto.record;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record Id(@JacksonXmlProperty(isAttribute = true, localName = "value") String value) {
}
