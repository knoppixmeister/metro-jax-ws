/*
 * $Id: WSDLDocument.java,v 1.1 2005-05-24 14:00:50 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import java.util.ArrayList;
import java.util.Iterator;
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
 * A WSDL document.
 *
 * @author JAX-RPC Development Team
 */
public class WSDLDocument extends AbstractDocument{

    public WSDLDocument() {
    }

    public Definitions getDefinitions() {
        return _definitions;
    }

    public void setDefinitions(Definitions d) {
        _definitions = d;
    }

    public Set collectAllNamespaces() {
        Set result = super.collectAllNamespaces();
        if (_definitions.getTargetNamespaceURI() != null) {
            result.add(_definitions.getTargetNamespaceURI());
        }
        return result;
    }

    public QName[] getAllServiceQNames() {

        ArrayList serviceQNames = new ArrayList();

        for (Iterator iter = getDefinitions().services(); iter.hasNext();) {
            Service next = (Service) iter.next();
            String targetNamespace = next.getDefining().getTargetNamespaceURI();
            String localName = next.getName();
            QName serviceQName = new QName(targetNamespace, localName);
            serviceQNames.add(serviceQName);
        }
        return (QName[]) serviceQNames.toArray(new QName[serviceQNames.size()]);
    }

    public QName[] getAllPortQNames() {
        ArrayList portQNames = new ArrayList();

        for (Iterator iter = getDefinitions().services(); iter.hasNext();) {
            Service next = (Service) iter.next();
            //Iterator ports = next.ports();
            for (Iterator piter = next.ports(); piter.hasNext();) {
                // If it's a relative import
                Port pnext = (Port) piter.next();
                String targetNamespace =
                    pnext.getDefining().getTargetNamespaceURI();
                String localName = pnext.getName();
                QName portQName = new QName(targetNamespace, localName);
                portQNames.add(portQName);
            }
        }
        return (QName[]) portQNames.toArray(new QName[portQNames.size()]);
    }

    public QName[] getPortQNames(String serviceNameLocalPart) {

        ArrayList portQNames = new ArrayList();

        for (Iterator iter = getDefinitions().services(); iter.hasNext();) {
            Service next = (Service) iter.next();
            if (next.getName().equals(serviceNameLocalPart)) {
                for (Iterator piter = next.ports(); piter.hasNext();) {
                    Port pnext = (Port) piter.next();
                    String targetNamespace =
                        pnext.getDefining().getTargetNamespaceURI();
                    String localName = pnext.getName();
                    QName portQName = new QName(targetNamespace, localName);
                    portQNames.add(portQName);
                }
            }
        }
        return (QName[]) portQNames.toArray(new QName[portQNames.size()]);
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        _definitions.accept(visitor);
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
        return _definitions;
    }

    private Definitions _definitions;

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
