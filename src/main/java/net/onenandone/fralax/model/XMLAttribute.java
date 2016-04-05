package net.onenandone.fralax.model;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


/**
 * @version 1.0
 * @author <a href="mailto:daniel.draper@1und1.de">Daniel Draper</a>
 */
@AllArgsConstructor
@EqualsAndHashCode
public class XMLAttribute implements XPathResult{

    @Getter
    private String name;
    @Getter
    private String value;


    public String prettyPrint() {
       return (name + " : " + value);
    }
}
