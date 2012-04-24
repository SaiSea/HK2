/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.qualifier;

import org.junit.Ignore;
import org.glassfish.hk2.Factory;
import org.glassfish.hk2.BinderFactory;
import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Module;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;

import static org.junit.Assert.*;

/**
 * Main test class, creates the HK2 services instance and insure that
 * all injections were successful.
 *
 * @author Jerome Dochez
 */

public class QualifierTest {

    @Test
    public void assertInjectionsQualifiedBoundBeforeUnqualified() {
        Habitat services = (Habitat) HK2.get().create(null, new Module() {
            @Override
            public void configure(BinderFactory binderFactory) {
                binderFactory.bind().to(QualifierInjectionTarget.class);
                // binding definition order seems to matter (see the other version of the same test
                binderFactory.bind(ToBeQualified.class).annotatedWith(SomeQualifier.class).to(QualifiedService.class);
                binderFactory.bind(ToBeQualified.class).to(FallbackService.class);
                binderFactory.bind(ToBeQualified.class).named("named").to(NamedService.class);
            }
        });
        
        QualifierInjectionTarget tests = services.byType(QualifierInjectionTarget.class).get();
        
        assertInjectedInstance(NamedService.class, tests.namedQualified);
        assertInjectedInstance(QualifiedService.class, tests.qualified);
        assertInjectedInstance(FallbackService.class, tests.fallback);

        assertInjectedProvider(NamedService.class, tests.namedQualifiedProvider);
        assertInjectedProvider(QualifiedService.class, tests.qualifiedProvider);
        assertInjectedProvider(FallbackService.class, tests.fallbackProvider);        
    }
    
    @Test
    public void assertInjectionsUnqualifiedBoundBeforeQualified() {
        Habitat services = (Habitat) HK2.get().create(null, new Module() {
            @Override
            public void configure(BinderFactory binderFactory) {
                binderFactory.bind().to(QualifierInjectionTarget.class);
                // binding definition order seems to matter (see the other version of the same test
                binderFactory.bind(ToBeQualified.class).to(FallbackService.class);
                binderFactory.bind(ToBeQualified.class).annotatedWith(SomeQualifier.class).to(QualifiedService.class);
                binderFactory.bind(ToBeQualified.class).named("named").to(NamedService.class);
            }
        });
        
        QualifierInjectionTarget tests = services.byType(QualifierInjectionTarget.class).get();
        
        assertInjectedInstance(NamedService.class, tests.namedQualified);
        assertInjectedInstance(QualifiedService.class, tests.qualified);
        assertInjectedInstance(FallbackService.class, tests.fallback);

        assertInjectedProvider(NamedService.class, tests.namedQualifiedProvider);
        assertInjectedProvider(QualifiedService.class, tests.qualifiedProvider);
        assertInjectedProvider(FallbackService.class, tests.fallbackProvider);        
    }
    
    private <T> void assertInjectedInstance(Class<? extends T> expectedType, T instance) {
        assertNotNull(instance);
        assertEquals(expectedType.getSimpleName(), instance.getClass().getSimpleName());        
    }
    
    private <T> void assertInjectedProvider(Class<? extends T> expectedType, Factory<T> provider) {
        assertNotNull(provider);
        assertNotNull(provider.get());
        assertEquals(expectedType.getSimpleName(), provider.get().getClass().getSimpleName());
    }
}
