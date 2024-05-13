package de.zuellich.meal_planner.pinterest.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.annotation.Nullable;

/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagingInformation {

    private String cursor = "";

    private String next = "";

    public String getCursor() {
        return cursor;
    }

    // Nullable due to the fact that Jackson might write null because its found like that in the JSON
    public void setCursor(@Nullable final String cursor) {
        if (cursor != null) {
            this.cursor = cursor;
        } else {
            this.cursor = "";
        }
    }

    public String getNext() {
        return next;
    }

    // Nullable due to the fact that Jackson might write null because its found like that in the JSON
    public void setNext(@Nullable final String next) {
        if (next != null) {
            this.next = next;
        } else {
            this.next = "";
        }
    }
}
