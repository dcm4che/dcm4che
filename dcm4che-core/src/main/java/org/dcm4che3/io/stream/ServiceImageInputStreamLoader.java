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
 * Portions created by the Initial Developer are Copyright (C) 2019
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

package org.dcm4che3.io.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Open an image input stream based on the type of the input object.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class ServiceImageInputStreamLoader<T> implements ImageInputStreamLoader<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceImageInputStreamLoader.class);

    static {
        // Ensure that the default behaviour is available if the service loaders were not visible in classpath
        if( !isInputClassHandled(URI.class) ) {
            IIORegistry.getDefaultInstance().registerServiceProvider(new FileURIImageInputStreamSpi());
        }

        if (!isInputClassHandled(ImageInputStream.class)) {
            IIORegistry.getDefaultInstance().registerServiceProvider(new DelegateImageInputStreamSpi());
        }
    }


    @Override
    public ImageInputStream openStream(T input) throws IOException {
        if(input == null) {
            throw new IllegalArgumentException("ImageInputStream cannot be bound to null input");
        }

        ServiceRegistry.Filter filter = filterImageInputStreamByInputClass(input.getClass());
        Iterator<ImageInputStreamSpi> it = IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, filter,true);
        if(!it.hasNext()) {
            throw new IllegalArgumentException("No ImageInputStream SPI for type "+input.getClass() );
        }

        // Use the highest ordered SPI for this type
        ImageInputStreamSpi spi = it.next();
        return spi.createInputStreamInstance(input);
    }

    protected List<ImageInputStreamSpi> filterByType(Class type) {
        List<ImageInputStreamSpi> iisList = new ArrayList<>();

        Iterator<ImageInputStreamSpi> it = IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, true);
        while (it.hasNext()) {
            ImageInputStreamSpi creator = it.next();
            if(creator.getInputClass().isAssignableFrom(type)) {
                iisList.add(creator);
            }
        }

        return iisList;
    }

    public static ServiceRegistry.Filter filterImageInputStreamByInputClass(Class inputClass) {
        return provider -> isImageInputStreamSpi(provider) && ((ImageInputStreamSpi)provider).getInputClass().isAssignableFrom(inputClass);
    }

    private static boolean isImageInputStreamSpi(Object provider) {
        return provider instanceof ImageInputStreamSpi;
    }

    private static boolean isInputClassHandled(Class inputClass) {
        ServiceRegistry.Filter filter = filterImageInputStreamByInputClass(inputClass);
        Iterator<ImageInputStreamSpi> it = IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, filter,false);
        return it.hasNext();

    }
}
