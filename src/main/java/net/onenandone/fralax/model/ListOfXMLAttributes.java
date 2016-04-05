package net.onenandone.fralax.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 * Wrapper Class for a List of Attributes as this can be returned as an XpathSearchResult.
 */
@AllArgsConstructor
public class ListOfXMLAttributes implements XPathResult {

    @Getter
    private final List<XMLAttribute> attributeList;
}
