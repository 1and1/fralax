package net.onenandone.fralax.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 *         Created on 01.04.16.
 * @version 1.0
 */
@AllArgsConstructor
public class ListOfXMLElements implements XPathResult {

    @Getter
    private final List<XMLElement> elementList;

    public Optional<XMLElement> getFirstElement(String attributeName, String value) {
        for (XMLElement element: elementList) {
            Optional<XMLAttribute> attribute = element.getAttribute(attributeName);
            if (attribute.isPresent()) {
                if (attribute.get().getValue().equals(value)) {
                    return Optional.of(element);
                }
            }
        }
        return Optional.empty();
    }

    public ListOfXMLElements getAllElements(String attributeName, String value) {
        ListOfXMLElements result = new ListOfXMLElements(new ArrayList<>());
        for (XMLElement element: elementList) {
            Optional<XMLAttribute> attribute = element.getAttribute(attributeName);
            if (attribute.isPresent()) {
                if (attribute.get().getValue().equals(value)) {
                    result.getElementList().add(element);
                }
            }
        }
        return result;
    }
}
