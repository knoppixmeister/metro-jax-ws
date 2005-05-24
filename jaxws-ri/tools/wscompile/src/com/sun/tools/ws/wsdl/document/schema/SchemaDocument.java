/*
 * $Id: SchemaDocument.java,v 1.1 2005-05-24 13:58:13 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.schema;

import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.AbstractDocument;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityAction;
import com.sun.tools.ws.wsdl.framework.EntityReferenceAction;
import com.sun.tools.ws.wsdl.framework.EntityReferenceValidator;
import com.sun.tools.ws.wsdl.framework.GloballyKnown;
import com.sun.tools.ws.wsdl.framework.Kind;
import com.sun.tools.ws.wsdl.framework.NoSuchEntityException;
import com.sun.tools.ws.wsdl.framework.ValidationException;

/**
 * A XML Schema document.
 *
 * @author JAX-RPC Development Team
 */
public class SchemaDocument extends AbstractDocument {

    public SchemaDocument() {
    }

    public Schema getSchema() {
        return _schema;
    }

    public void setSchema(Schema s) {
        _schema = s;
    }

    public Set collectAllNamespaces() {
        Set result = super.collectAllNamespaces();
        if (_schema.getTargetNamespaceURI() != null) {
            result.add(_schema.getTargetNamespaceURI());
        }
        return result;
    }

    public void validate(EntityReferenceValidator validator) {
        GloballyValidatingAction action =
            new GloballyValidatingAction(this, validator);
        withAllSubEntitiesDo(action);
        if (action.getException() != null) {
            throw action.getException();
        }
    }

    protected Entity getRoot() {
        return _schema;
    }

    private Schema _schema;

    private class GloballyValidatingAction
        implements EntityAction, EntityReferenceAction {
        public GloballyValidatingAction(
            AbstractDocument document,
            EntityReferenceValidator validator) {
            _document = document;
            _validator = validator;
        }

        public void perform(Entity entity) {
            try {
                entity.validateThis();
                entity.withAllEntityReferencesDo(this);
                entity.withAllSubEntitiesDo(this);
            } catch (ValidationException e) {
                if (_exception == null) {
                    _exception = e;
                }
            }
        }

        public void perform(Kind kind, QName name) {
            try {
                GloballyKnown entity = _document.find(kind, name);
            } catch (NoSuchEntityException e) {
                // failed to resolve, check with the validator
                if (_exception == null) {
                    if (_validator == null
                        || !_validator.isValid(kind, name)) {
                        _exception = e;
                    }
                }
            }
        }

        public ValidationException getException() {
            return _exception;
        }

        private ValidationException _exception;
        private AbstractDocument _document;
        private EntityReferenceValidator _validator;
    }
}
