/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.tests.locator.destroy;

import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;
import org.jvnet.hk2.internal.Pretty;

/**
 * @author jwells
 *
 */
public class DestroyTest {
    private final static String TEST_NAME = "DestroyTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new DestroyModule());
    
    /**
     * Tests that things are destroyed in opposite order from where they began
     */
    @Test
    public void testDestructionOrder() {
        ServiceHandle<Foo> fooHandle = locator.getServiceHandle(Foo.class);
        Assert.assertNotNull(fooHandle);
        
        Foo foo = fooHandle.getService();
        Assert.assertNotNull(foo);
        
        // Kill them all!  Mwmuaaaaaa
        fooHandle.destroy();
        
        Registrar registrar = locator.getService(Registrar.class);
        
        List<Object> births = registrar.getBirths();
        List<Object> deaths = registrar.getDeaths();
        
        Assert.assertEquals("Did not get all the postConstructs we expected: " + Pretty.collection(births), 4, births.size());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Qux.class, births.get(0).getClass());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Baz.class, births.get(1).getClass());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Bar.class, births.get(2).getClass());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Foo.class, births.get(3).getClass());
        
        Assert.assertEquals("Did not get all the preDestroys we expected: " + Pretty.collection(deaths), 4, deaths.size());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Qux.class, deaths.get(3).getClass());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Baz.class, deaths.get(2).getClass());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Bar.class, deaths.get(1).getClass());
        Assert.assertEquals("Got invalid birth order: " + Pretty.collection(births), Foo.class, deaths.get(0).getClass());
    }

}
