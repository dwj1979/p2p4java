/*
 * Copyright (c) 2002-2007 Sun Microsystems, Inc.  All rights reserved.
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

package edu.uci.ics.luci.p2p4java.impl.access.simpleACL;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import java.net.URISyntaxException;

import junit.framework.*;

// import edu.uci.ics.luci.p2p4java.peergroup.PeerGroupFactory;

import org.junit.Ignore;

import edu.uci.ics.luci.p2p4java.access.AccessService;
import edu.uci.ics.luci.p2p4java.access.AccessService.AccessResult;
import edu.uci.ics.luci.p2p4java.credential.Credential;
import edu.uci.ics.luci.p2p4java.credential.PrivilegedOperation;
import edu.uci.ics.luci.p2p4java.document.AdvertisementFactory;
import edu.uci.ics.luci.p2p4java.document.Element;
import edu.uci.ics.luci.p2p4java.document.MimeMediaType;
import edu.uci.ics.luci.p2p4java.document.StructuredDocumentFactory;
import edu.uci.ics.luci.p2p4java.document.XMLDocument;
import edu.uci.ics.luci.p2p4java.document.XMLElement;
import edu.uci.ics.luci.p2p4java.id.ID;
import edu.uci.ics.luci.p2p4java.id.IDFactory;
import edu.uci.ics.luci.p2p4java.impl.access.simpleACL.SimpleACLAccessService;
import edu.uci.ics.luci.p2p4java.impl.peergroup.StdPeerGroupParamAdv;
import edu.uci.ics.luci.p2p4java.membership.MembershipService;
import edu.uci.ics.luci.p2p4java.peergroup.PeerGroup;
import edu.uci.ics.luci.p2p4java.peergroup.WorldPeerGroupFactory;
import edu.uci.ics.luci.p2p4java.platform.ModuleSpecID;
import edu.uci.ics.luci.p2p4java.protocol.ModuleImplAdvertisement;
import edu.uci.ics.luci.p2p4java.protocol.PeerGroupAdvertisement;

@Ignore("JXTA Configurator & PeerGroupFactory Required")
public class SimpleACLAccessServiceTest extends TestCase {

    static PeerGroup npg = null;
    static PeerGroup pg = null;

    public SimpleACLAccessServiceTest(java.lang.String testName) {
        super(testName);

        synchronized (SimpleACLAccessServiceTest.class) {
            try {
                if (null == npg) {
                    final PeerGroup wpg = new WorldPeerGroupFactory().getWorldPeerGroup();
                    
                    npg = null; //PeerGroupFactory.newNetPeerGroup(wpg);

                    ModuleImplAdvertisement newGroupImpl = npg.getAllPurposePeerGroupImplAdvertisement();

                    StdPeerGroupParamAdv params = new StdPeerGroupParamAdv(newGroupImpl.getParam());

                    Map services = params.getServices();

                    ModuleImplAdvertisement aModuleAdv = (ModuleImplAdvertisement) services.get(PeerGroup.accessClassID);

                    services.remove(PeerGroup.accessClassID);

                    ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement)
                            AdvertisementFactory.newAdvertisement(ModuleImplAdvertisement.getAdvertisementType());

                    implAdv.setModuleSpecID(SimpleACLAccessService.simpleACLAccessSpecID);
                    implAdv.setCompat(aModuleAdv.getCompat());
                    implAdv.setCode(SimpleACLAccessService.class.getName());
                    implAdv.setUri(aModuleAdv.getUri());
                    implAdv.setProvider(aModuleAdv.getProvider());
                    implAdv.setDescription("Simple ACL Access Service");

                    // replace it
                    services.put(PeerGroup.accessClassID, implAdv);

                    newGroupImpl.setParam((Element) params.getDocument(MimeMediaType.XMLUTF8));

                    if (!newGroupImpl.getModuleSpecID().equals(PeerGroup.allPurposePeerGroupSpecID)) {
                        newGroupImpl.setModuleSpecID(IDFactory.newModuleSpecID(newGroupImpl.getModuleSpecID().getBaseClass()));
                    } else {
                        try {
                            ID simpleACLGrpModSpecID = ID.create(
                                    new URI(ID.URIEncodingName, "jxta:uuid-" + "DEADBEEFDEAFBABAFEEDBABE000000010406", null));

                            newGroupImpl.setModuleSpecID((ModuleSpecID) simpleACLGrpModSpecID);
                        } catch (URISyntaxException absurd) {// Fall through.
                        }
                    }

                    npg.getDiscoveryService().publish(newGroupImpl, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);

                    npg.getDiscoveryService().remotePublish(newGroupImpl, PeerGroup.DEFAULT_LIFETIME);

                    PeerGroupAdvertisement newPGAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                            PeerGroupAdvertisement.getAdvertisementType());

                    newPGAdv.setPeerGroupID(IDFactory.newPeerGroupID());
                    newPGAdv.setModuleSpecID(newGroupImpl.getModuleSpecID());
                    newPGAdv.setName("Test Group");
                    newPGAdv.setDescription("Created by Unit Test");

                    XMLDocument accessparams = (XMLDocument) StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8
                            ,
                            "Parm");

                    XMLElement perm = accessparams.createElement("perm", "<<DEFAULT>>:nobody,permit");

                    accessparams.appendChild(perm);

                    perm = accessparams.createElement("perm", "everyone:<<ALL>>");
                    accessparams.appendChild(perm);

                    perm = accessparams.createElement("perm", "permit:nobody,permit,allow");
                    accessparams.appendChild(perm);

                    perm = accessparams.createElement("perm", "deny:notpermit,notallow");
                    accessparams.appendChild(perm);

                    newPGAdv.putServiceParam(PeerGroup.accessClassID, accessparams);

                    npg.getDiscoveryService().publish(newPGAdv, PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);

                    npg.getDiscoveryService().remotePublish(newPGAdv, PeerGroup.DEFAULT_LIFETIME);

                    pg = npg.newGroup(newPGAdv);
                }
            } catch (Throwable all) {
                all.printStackTrace();
                fail("exception thrown : " + all.getMessage());
            }
        }
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());

        synchronized (SimpleACLAccessServiceTest.class) {
            if (null != pg) {
                pg.stopApp();
//                pg.unref();
                pg = null;
            }

            if (null != npg) {
                npg.stopApp();
//                npg.unref();
                npg = null;
            }
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleACLAccessServiceTest.class);

        return suite;
    }

    public void testAllow() {
        try {
            AccessService access = pg.getAccessService();
            MembershipService membership = pg.getMembershipService();

            Credential cred = membership.getDefaultCredential();

            PrivilegedOperation allowed = access.newPrivilegedOperation("permit", cred);

            assertTrue("Operation should be allowed", AccessResult.PERMITTED == access.doAccessCheck(allowed, cred));
        } catch (Exception caught) {
            caught.printStackTrace();
            fail("exception thrown : " + caught.getMessage());
        }
    }

    public void testDefault() {
        try {
            AccessService access = pg.getAccessService();
            MembershipService membership = pg.getMembershipService();

            Credential cred = membership.getDefaultCredential();

            PrivilegedOperation allowed = access.newPrivilegedOperation("apermissionwhichijustmadeup", cred);

            assertTrue("Operation should be allowed", AccessResult.PERMITTED == access.doAccessCheck(allowed, cred));
        } catch (Exception caught) {
            caught.printStackTrace();
            fail("exception thrown : " + caught.getMessage());
        }
    }

    public void testDeny() {
        try {
            AccessService access = pg.getAccessService();
            MembershipService membership = pg.getMembershipService();

            Credential cred = membership.getDefaultCredential();

            PrivilegedOperation denied = access.newPrivilegedOperation("deny", cred);

            assertTrue("Operation should be denied", AccessResult.DISALLOWED == access.doAccessCheck(denied, cred));

            StringWriter serialed = new StringWriter();

            ((XMLDocument) denied.getDocument(MimeMediaType.XMLUTF8)).sendToWriter(serialed);

            Reader deserial = new StringReader(serialed.toString());

            PrivilegedOperation redenied = access.newPrivilegedOperation(
                    StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, deserial));

            assertTrue("Operation should be denied", AccessResult.DISALLOWED == access.doAccessCheck(redenied, cred));
        } catch (Exception caught) {
            caught.printStackTrace();
            fail("exception thrown : " + caught.getMessage());
        }
    }
}
