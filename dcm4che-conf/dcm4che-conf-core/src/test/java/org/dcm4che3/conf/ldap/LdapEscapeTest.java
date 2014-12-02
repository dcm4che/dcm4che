/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.ldap;

import org.junit.Test;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * @author Roman K
 */
public class LdapEscapeTest {

    @Test
    public void testEscaping() throws NamingException {
        if (System.getProperty("ldap") != null) {
            Hashtable<String, String> env = new Hashtable<String, String>();

            //local
            env.put("java.naming.provider.url", "ldap://localhost:389/dc=example,dc=com");

            //slapd
            env.put("java.naming.security.principal", "cn=Manager,dc=example,dc=com");

            //opendj
            //env.put("java.naming.security.principal", "cn=Directory Manager");


            // apache ds
            //env.put("java.naming.security.principal", "uid=admin,ou=system");

            // COMMON PROPS
            env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
            env.put("java.naming.ldap.attributes.binary", "dicomVendorData");
            env.put("java.naming.security.credentials", "secret");



            env = (Hashtable) env.clone();
            String e = (String) env.get("java.naming.provider.url");
            int end = e.lastIndexOf(47);
            env.put("java.naming.provider.url", e.substring(0, end));
            String baseDN = e.substring(end + 1);
            InitialDirContext ldapCtx = new InitialDirContext(env);

            String noteTitle = "(110514, DCM, \"Incorrect worklist entry selected\")";

            String dn = "dcmRejectionNoteTitle="+noteTitle.replace(",","\\,").replace("\"","\\\"")+ ',' + baseDN;

            Attributes attrs = new BasicAttributes();
            attrs.put("dcmRejectionNoteTitle", noteTitle);
            attrs.put("cn", "someCn");
            attrs.put("objectClass", "dcmRejectionNote");

            ldapCtx.createSubcontext(dn, attrs);


        }
    }
}
