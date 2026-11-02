package com.startupidea.yipshare.item;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

import java.util.Set;

public class InvalidItem extends AppException {

    private final Set<ItemField> fields;

    public InvalidItem(Set<ItemField> fields) {
        super("Invalid item fields: " + fields, R.string.error_campo_obligatorio);
        this.fields = fields;
    }

    public Set<ItemField> getFields() {
        return fields;
    }
}
