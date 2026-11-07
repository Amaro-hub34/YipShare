package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

import java.util.Set;

public class InvalidCondominium extends AppException {

    private final Set<CondominiumField> fields;

    public InvalidCondominium(Set<CondominiumField> fields) {
        super("Invalid condominium fields: " + fields, R.string.error_campo_obligatorio);
        this.fields = fields;
    }

    public Set<CondominiumField> getFields() {
        return fields;
    }
}
