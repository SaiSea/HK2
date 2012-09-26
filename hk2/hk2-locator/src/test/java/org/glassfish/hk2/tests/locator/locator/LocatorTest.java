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
package org.glassfish.hk2.tests.locator.locator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Ignore;
import org.junit.Test;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * @author jwells
 *
 */
public class LocatorTest {
    private final static String TEST_NAME = "LocatorTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new LocatorModule());
    
    /* package */ final static String COBOL_ID = "COBOL is fun!";
    /* package */ final static int FORTRAN = 77;  // There really was a Fortran-77!
    
    /**
     * Gets all the services in the registry
     */
    @Test
    public void testGetAllServices() {
        
        List<AdminCommand> handles = locator.getAllServices(AdminCommand.class);
        Assert.assertNotNull(handles);
        
        Assert.assertTrue("Expected all three handles, but got " + handles.size(), handles.size() == 3);
        
        // Now get the specific items that should be in the set, and verify they are all there
        BootCommand bootCommand = (BootCommand) locator.getService(AdminCommand.class, "BootCommand");
        GetStatisticsCommand statsCommand = (GetStatisticsCommand) locator.getService(AdminCommand.class, "GetStatisticsCommand");
        ShutdownCommand shutdownCommand = (ShutdownCommand) locator.getService(AdminCommand.class, "ShutdownCommand");
        
        Assert.assertNotNull(bootCommand);
        Assert.assertNotNull(statsCommand);
        Assert.assertNotNull(shutdownCommand);
        
        Assert.assertTrue("Returned services did not contain BootCommand " + Pretty.collection(handles), handles.contains(bootCommand));
        Assert.assertTrue("Returned services did not contain StatsCommand " + Pretty.collection(handles), handles.contains(statsCommand));
        Assert.assertTrue("Returned services did not contain ShtudownCommand " + Pretty.collection(handles), handles.contains(shutdownCommand));
    }
    
    /**
     * Gets all the services with a filter
     */
    @Test
    public void testGetAllServicesWithFilter() {
        
        List<?> handles = locator.getAllServices(new Filter() {

            @Override
            public boolean matches(Descriptor d) {
                return true;
            }
            
        });
        Assert.assertNotNull(handles);
        
        Assert.assertTrue("Expected at least four handles, but got " + handles.size(), handles.size() >= 4);
        
        // Now get the specific items that should be in the set, and verify they are all there
        BootCommand bootCommand = (BootCommand) locator.getService(AdminCommand.class, "BootCommand");
        GetStatisticsCommand statsCommand = (GetStatisticsCommand) locator.getService(AdminCommand.class, "GetStatisticsCommand");
        ShutdownCommand shutdownCommand = (ShutdownCommand) locator.getService(AdminCommand.class, "ShutdownCommand");
        ServiceLocator locatorItself = (ServiceLocator) locator.getService(ServiceLocator.class);
        
        Assert.assertTrue("Returned services did not contain BootCommand " + Pretty.collection(handles), handles.contains(bootCommand));
        Assert.assertTrue("Returned services did not contain StatsCommand " + Pretty.collection(handles), handles.contains(statsCommand));
        Assert.assertTrue("Returned services did not contain ShtudownCommand " + Pretty.collection(handles), handles.contains(shutdownCommand));
        Assert.assertTrue("Returned services did not contain ServiceLocator " + Pretty.collection(handles), handles.contains(locatorItself));
    }
    
    /**
     * Gets a service from a Foreign descriptor (a descriptor not created with BuilderHelper)
     */
    @Test
    public void testForeignDescriptor() {
        FrenchService fs = locator.getService(FrenchService.class);
        Assert.assertNotNull(fs);
    }
    
    /**
     * Gets a service from a Foreign descriptor (a descriptor not created with BuilderHelper)
     */
    @Test
    public void testReifyForeignDescriptorDirectly() {
        ForeignDescriptor fd = new ForeignDescriptor();
        fd.setImplementation(FrenchService.class.getName());
        
        Set<String> contracts = fd.getAdvertisedContracts();
        contracts.add(FrenchService.class.getName());
        
        ActiveDescriptor<?> ad = locator.reifyDescriptor(fd);
        Assert.assertEquals(FrenchService.class, ad.getImplementationClass());
        
        Assert.assertTrue(ad.getContractTypes().contains(FrenchService.class));
    }
    
    /**
     * Gets a service from a Foreign descriptor (a descriptor not created with BuilderHelper)
     */
    @Test
    public void testReifyForeignActiveDescriptorDirectly() {
        HashSet<Type> contracts = new HashSet<Type>();
        contracts.add(GermanService.class);
        
        Set<Annotation> qualifiers = Collections.emptySet();
        
        ForeignActiveDescriptor<GermanService> fad = new ForeignActiveDescriptor<GermanService>(
                contracts,
                PerLookup.class,
                null,
                qualifiers,
                DescriptorType.CLASS,
                0,
                GermanService.class);
        
        ActiveDescriptor<?> ad = locator.reifyDescriptor(fad);
        Assert.assertEquals(GermanService.class, ad.getImplementationClass());
        
        Assert.assertTrue(ad.getContractTypes().contains(GermanService.class));
    }
    
    /**
     * Gets services based on their qualifier
     */
    @Test
    public void testLookupViaQualifier() {
        List<Object> deadLanguages = locator.getAllServices(Dead.class);
        Assert.assertNotNull(deadLanguages);
        
        Assert.assertTrue("Expected at least two dead languages, but got " + deadLanguages.size(),
                deadLanguages.size() >= 2);
        
        // Should be in priority order
        Assert.assertTrue(deadLanguages.get(0) instanceof LatinService);
        Assert.assertTrue(deadLanguages.get(1) instanceof ThracianService);
    }
    
    /**
     * Gets the per-lookup services
     */
    @Test
    public void testLookupViaPerLookup() {
        List<Object> perLookup = locator.getAllServices(PerLookup.class);
        Assert.assertNotNull(perLookup);
        
        Assert.assertTrue("Expected at least five perLookup services, but got " + perLookup.size(),
                perLookup.size() >= 5);
    }
    
    /**
     * Gets the singleton services
     */
    @Test
    public void testLookupViaPerSingleton() {
        List<Object> singleton = locator.getAllServices(Singleton.class);
        Assert.assertNotNull(singleton);
        
        Assert.assertTrue("Expected at least one singleton services, but got " + singleton.size(),
                singleton.size() >= 1);
    }
    
    /**
     * Tests that we can look up using TypeLiterals
     */
    @Test
    public void testLookupWithTypeLiteral() {
        ComputerLanguage<String> cobol = locator.getService((new TypeLiteral<ComputerLanguage<String>>() {}).getType());
        Assert.assertNotNull(cobol);
        
        Assert.assertSame(COBOL_ID, cobol.getItem());
        
        ComputerLanguage<Integer> fortran = locator.getService((new TypeLiteral<ComputerLanguage<Integer>>() {}).getType());
        Assert.assertNotNull(fortran);
        
        Integer i = fortran.getItem();
        Assert.assertSame("fortran.getItem() is " + fortran.getItem(), FORTRAN, i.intValue());
    }
    
    /**
     * This binds a non-reified active descriptor with contracts that are not contracts,
     * and qualifiers that are not qualifiers, and makes sure that we can look it up
     * ONLY with the things we put into the ActiveDescriptor as contracts or qualifiers
     */
    @Test
    public void testBindEverythingBackwards() {
        HashSet<Type> contracts = new HashSet<Type>();
        contracts.add(IsNotAContract.class);  // Crazy, I know
        // And the thing marked @Contract is NOT added
        
        NotAQualifier naq = new NotAQualifierImpl();
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.add(naq);  // Crazy, I know
        // And the thing marked @Qualifier is NOT added
        
        ForeignActiveDescriptor<ChineseService> fad = new ForeignActiveDescriptor<ChineseService>(
                contracts,
                PerLookup.class,
                null,
                qualifiers,
                DescriptorType.CLASS,
                0,
                ChineseService.class);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, fad);
        
        Assert.assertNotNull(locator.getService(IsNotAContract.class));
        Assert.assertNull(locator.getService(IsAContract.class));
        
        Assert.assertNotNull(locator.getService(IsNotAContract.class, naq));
        Assert.assertNull(locator.getService(IsNotAContract.class, new DeadImpl()));
    }
    
    /**
     * Tests that a class with no scope will take on the scope
     * given to it in a binding
     */
    @Test
    public void testNakedScope() {
        List<ActiveDescriptor<?>> nakedScopeServices =
                locator.getDescriptors(BuilderHelper.createContractFilter(
                        NoScopeService.class.getName()));
        
        Assert.assertTrue("Size should be 2, but is " + nakedScopeServices.size(),
                nakedScopeServices.size() == 2);
        
        ActiveDescriptor<?> singleton = nakedScopeServices.get(0);
        ActiveDescriptor<?> perLookup = nakedScopeServices.get(1);
        
        singleton = locator.reifyDescriptor(singleton);
        perLookup = locator.reifyDescriptor(perLookup);
        
        Assert.assertEquals(Singleton.class, singleton.getScopeAnnotation());
        Assert.assertEquals(PerLookup.class, perLookup.getScopeAnnotation());
    }
    
    private final static int BIG_NUMBER = 100000;
    
    /**
     * This test assumes that the cached version of the lookup is *much* faster
     * than the non-cached version, which actually seems to be true.  For information
     * the results are printed out
     */
    @Test // @Ignore
    public void testPerformance() {
        // The Filter version of getAllServices should be a lot slower than the
        // cache version
        
        // First just look it up to create it and initialize the cache
        Assert.assertNotNull(locator.getService(PerformanceService.class));
        Filter f = BuilderHelper.createContractFilter(PerformanceService.class.getName());
        
        long filterElapsedTime = System.currentTimeMillis();
        for (int lcv = 0; lcv < BIG_NUMBER; lcv++) {
            locator.getAllServices(f);
        }
        filterElapsedTime = System.currentTimeMillis() - filterElapsedTime;
        
        long cacheElapsedTime = System.currentTimeMillis();
        for (int lcv = 0; lcv < BIG_NUMBER; lcv++) {
            locator.getAllServices(PerformanceService.class);
        }
        cacheElapsedTime = System.currentTimeMillis() - cacheElapsedTime;
        
        Assert.assertTrue("The non-cached (" + filterElapsedTime + ") was faster than cached (" +
            cacheElapsedTime + ")", filterElapsedTime > cacheElapsedTime);
        
        System.out.println("Non-cached time: " + filterElapsedTime + " cached time: " + cacheElapsedTime +
                " savings of: " + (filterElapsedTime - cacheElapsedTime));
        
    }

}
