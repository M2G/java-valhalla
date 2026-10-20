package org.example.parser;

import java.io.InputStream;
import java.util.List;

public interface XmlParser<T> {
    List<T> parse(InputStream xmlStream);
}
