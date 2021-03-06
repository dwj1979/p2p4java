/*
 * ====================================================================
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 *  The Sun Project JXTA(TM) Software License
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include the following acknowledgment: "This product includes software
 *     developed by Sun Microsystems, Inc. for JXTA(TM) technology."
 *     Alternately, this acknowledgment may appear in the software itself, if
 *     and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *     not be used to endorse or promote products derived from this software
 *     without prior written permission. For written permission, please contact
 *     Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA", nor may
 *     "JXTA" appear in their name, without prior written permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SUN
 *  MICROSYSTEMS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  JXTA is a registered trademark of Sun Microsystems, Inc. in the United
 *  States and other countries.
 *
 *  Please see the license information page at :
 *  <http://www.jxta.org/project/www/license.html> for instructions on use of
 *  the license in source files.
 *
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many individuals
 *  on behalf of Project JXTA. For more information on Project JXTA, please see
 *  http://www.jxta.org.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 */

package edu.uci.ics.luci.jxse.impl.OSGi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.launch.Framework;

import edu.uci.ics.luci.jxse.JxseInstantiator;
import edu.uci.ics.luci.jxse.OSGi.JxseOSGiFrameworkLauncher;
import edu.uci.ics.luci.p2p4java.configuration.JxtaConfiguration;
import edu.uci.ics.luci.p2p4java.configuration.PropertiesUtil;

/**
 * Felix OSGi framework launcher.
 *
 * <p>Properties are read from the {@code Felix.properties} file.
 *
 * @deprecated Since 2.7 - This implementation creates a dependency to the Felix
 * framework. A better implementation of OSGi is under preparation.
 */
@Deprecated
public class JxseOSGiFelixFrameworkLauncher implements JxseOSGiFrameworkLauncher {

    /**
     *  Logger
     */
    private final static Logger LOG = Logger.getLogger(JxseOSGiFelixFrameworkLauncher.class.getName());
    
    /**
     *  OSGi configuration and properties
     */
    private static final JxtaConfiguration Configuration = new JxtaConfiguration();

    // Loading OSGi configuration
    static {
        InputStream Tmp = null;
        try {
            Tmp = JxseOSGiFelixFrameworkLauncher.class.getResourceAsStream("Felix.properties");
            Configuration.load(Tmp);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Cannot load Felix.properties :\n{0}", ex.toString());
        } finally {
            if ( Tmp != null ) {
                try {
                    Tmp.close();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Can't close Felix.properties input stream :\n{0}", ex.toString());
                }
            }
        }
    }

    /**
     * OSGI Felix framework instance
     */
    public static final Framework INSTANCE;

    // Creating the framework instance
    static {

        // Creating configuration elements
        Map configMap = new HashMap(0);

        // For bundles/services to automatically activate
        List list = new ArrayList();

        // Preparing OSGi configuration
        for (String Item : PropertiesUtil.stringPropertyNames(Configuration)) {

            if (Item.startsWith(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP)) {

                // Adding an OSGi bundle/service to start
                String Temp = Configuration.getProperty(Item);
                Class TempCl = JxseInstantiator.forName(Temp);
                list.add(JxseInstantiator.instantiateWithNoParameterConstructor(TempCl));

            } else {

                // Registering the configuration
                configMap.put(Item, Configuration.getProperty(Item));

            }

        }

        // Registering the list
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

        // Creating the instance
        INSTANCE = new Felix(configMap);

    }

    public Framework getOsgiFrameworkInstance() {

        return INSTANCE;

    }

}
