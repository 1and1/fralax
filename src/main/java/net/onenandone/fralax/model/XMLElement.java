package net.onenandone.fralax.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 */
@RequiredArgsConstructor
public class XMLElement implements XPathResult {
    @Getter
    private final String identifier;
    @Getter
    private List<XMLAttribute> attributes = new ArrayList<>();
    @Getter
    private List<XMLElement> children = new ArrayList<>();

    public Optional<XMLAttribute> getAttribute(String name) {
        for (XMLAttribute attribute: attributes) {
            if (attribute.getName().equals(name)) {
                return Optional.of(attribute);
            }
        }
        return null;
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("start XMLElement name: " + identifier + "\n");
        for (XMLAttribute attribute: attributes) {
            sb.append("\tAttribute: " + attribute.getName() + " value: " + attribute.getValue() + "\n");
        }
        for (XMLElement child : children) {
            sb.append(child.prettyPrint());
        }
        sb.append("END XMLELEMENT\n---------------------------\n");
        return sb.toString();
    }

}
