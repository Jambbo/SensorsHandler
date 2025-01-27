package com.example.datageneratormicroservice.config;

import com.jcabi.xml.XML;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class TextXPath {

    private final XML xml;
    private final String node;

    @Override
    public String toString() {
        List<XML> nodes = this.xml.nodes(this.node);
        if(nodes.isEmpty()){
            throw new IllegalArgumentException("XPath did not return any nodes for: "+ node);
        }
        return nodes
                .get(0)
                .xpath("text()")
                .get(0);
    }

}
