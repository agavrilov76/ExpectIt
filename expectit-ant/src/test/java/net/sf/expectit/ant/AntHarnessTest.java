package net.sf.expectit.ant;

/*
 * #%L
 * net.sf.expectit
 * %%
 * Copyright (C) 2014 Alexey Gavrilov
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.*;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.util.TreeSet;

import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.Parameters;

/**
 * A test harness for running Ant targets.
 */
@RunWith(Parameterized.class)
public class AntHarnessTest {

    private static FtpServer ftpServer;
    private static int ftpPort;

    private static SshServer sshServer;
    private static int sshPort;


    @BeforeClass
    public static void startFtpServer() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        BaseUser user = new BaseUser();
        user.setName("ftp");
        user.setPassword("secret");
        serverFactory.getUserManager().save(user);
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(0);
        Listener listener = factory.createListener();
        serverFactory.addListener("default", listener);
        ftpServer = serverFactory.createServer();
        ftpServer.start();
        ftpPort = listener.getPort();
    }

    @BeforeClass
    public static void startSshServer() throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        ServerSocket serverSocket = new ServerSocket(0);
        sshPort = serverSocket.getLocalPort();
        serverSocket.close();
        sshServer.setPort(sshPort);
        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return "ssh".equals(username) && "secret".equals(password);
            }
        });
        sshServer.setShellFactory(new SshEchoCommandFactory());
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.start();
    }

    @AfterClass
    public static void stopServers() throws FtpException, InterruptedException {
        ftpServer.stop();
        sshServer.stop();
    }

    private static File buildFile;

    private static void setupBuildFile() throws IOException {
        if (buildFile != null) {
            return;
        }
        buildFile = File.createTempFile("build", ".xml");
        buildFile.deleteOnExit();
        URL url = Resources.getResource("build-test.xml");
        Resources.copy(url, new FileOutputStream(buildFile));
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() throws IOException {
        Project project = newProject();
        Function<String, Object[]> toTargetArray = new Function<String, Object[]>() {
            @Override
            public Object[] apply(String input) {
                return new Object[]{input};
            }
        };
        Iterable<String> filtered = Iterables.filter(new TreeSet<String>(project.getTargets().keySet()),
                new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return !input.isEmpty();
                    }
                });
        return Iterables.transform(filtered, toTargetArray);
    }

    private static Project newProject() throws IOException {
        setupBuildFile();
        Project project = new Project();
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.init();
        DefaultLogger listener = new DefaultLogger();
        listener.setErrorPrintStream(System.err);
        listener.setOutputPrintStream(System.out);
        listener.setMessageOutputLevel(Project.MSG_INFO);
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projectHelper", helper);
        project.setProperty("ftp.port", String.valueOf(ftpPort));
        project.setProperty("ssh.port", String.valueOf(sshPort));
        helper.parse(project, buildFile);
        project.addBuildListener(listener);
        return project;
    }

    private final String target;

    public AntHarnessTest(String target) {
        this.target = target;
    }

    @Test
    public void runTarget() throws IOException {
        Project project = newProject();
        project.log("started: " + target);
        // prepare
        project.executeTarget("");
        boolean negative = target.endsWith("-negative");
        // run test
        try {
            project.executeTarget(target);
            if (negative) {
                fail("Negative test fails");
            }
        } catch (BuildException e) {
            e.printStackTrace();
            if (!negative) {
                fail("Positive test fails");
            }
        } finally {
            project.log("finished");
        }
    }

}
