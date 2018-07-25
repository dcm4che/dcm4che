/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests Bulkdata methods.
 * 
 * @author Bill Wallace <wayfarer3130@gmail.com>
 */
public class BulkdataTest {
    long offset = 3l*Integer.MAX_VALUE;
    long length = 0xFFFF0000l;
    String url = "file:///someFile.dcm";
    BulkData bulk = new BulkData(url,offset,(int) length,false);

    @Test
    public void testBulkdata_create() {
        assertEquals(url+"?offset="+offset+"&length="+((int) length),bulk.getURI());
        assertEquals(length,bulk.longLength());
        assertEquals(offset,bulk.offset());
        BulkData bulk2 = new BulkData(url,offset,(int) length, false);
        assertEquals(bulk,bulk2);
        assertEquals(bulk.hashCode(),bulk2.hashCode());
    }
    
    @Test
    public void testBulkdata_update() {
        BulkData bulkUnknown = new BulkData(url,-1l, -1, false);
        assertEquals(url+"?offset=-1&length=-1",bulkUnknown.getURI());
        assertEquals(-1,bulkUnknown.longLength());
        assertEquals(-1,bulkUnknown.offset());
        bulkUnknown.setLength(length);
        bulkUnknown.setOffset(offset);
        assertEquals(bulk,bulkUnknown);
        assertEquals(bulk.hashCode(),bulkUnknown.hashCode());
    }

}
