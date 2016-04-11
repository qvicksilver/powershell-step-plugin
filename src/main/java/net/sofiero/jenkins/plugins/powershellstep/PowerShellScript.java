package net.sofiero.jenkins.plugins.powershellstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.durabletask.DurableTaskDescriptor;
import org.jenkinsci.plugins.durabletask.FileMonitoringTask;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Runs a PowerShell script.
 */
public final class PowerShellScript extends FileMonitoringTask {

    private final String script;

    @DataBoundConstructor
    public PowerShellScript(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    @Override
    protected FileMonitoringTask.FileMonitoringController doLaunch(FilePath ws, Launcher launcher, TaskListener listener, EnvVars envVars) throws IOException, InterruptedException {
        if (launcher.isUnix()) {
            throw new IOException("PowerShell scripts can only be run on Windows nodes");
        }
        BatchController c = new BatchController(ws);
        
//        c.getBatchFile1(ws).write(String.format("call \"%s\" > \"%s\" 2>&1\r\necho %%ERRORLEVEL%% > \"%s\"\r\n",
//                c.getBatchFile2(ws),
//                c.getLogFile(ws),
//                c.getResultFile(ws)
//        ), "UTF-8");
        c.getBatchFile1(ws).write(String.format("powershell.exe \"& \"\"%s\"\"\" > \"%s\" 2>&1\r\necho %%ERRORLEVEL%% > \"%s\"\r\n",
                c.getBatchFile2(ws),
                c.getLogFile(ws),
                c.getResultFile(ws)
        ), "UTF-8");
        c.getBatchFile2(ws).write(script, "UTF-8");

        Launcher.ProcStarter ps = launcher.launch().cmds("cmd", "/c", "\"\"" + c.getBatchFile1(ws) + "\"\"").envs(envVars).pwd(ws).quiet(true);
        try {
            listener.getLogger().println("[" + ws.getRemote().replaceFirst("^.+\\\\", "") + "] Running PowerShell script"); // details printed by cmd            
        } catch (Exception x) { // ?
            x.printStackTrace(listener.getLogger());
        }
        ps.start();
        return c;
    }

    private static final class BatchController extends FileMonitoringTask.FileMonitoringController {

        private BatchController(FilePath ws) throws IOException, InterruptedException {
            super(ws);
        }

        public FilePath getBatchFile1(FilePath ws) {
            try {
                return controlDir(ws).child("jenkins-wrap.bat");
            } catch (IOException ex) {
                Logger.getLogger(PowerShellScript.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShellScript.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        public FilePath getBatchFile2(FilePath ws) {
            try {
                return controlDir(ws).child("jenkins-main.ps1");
            } catch (IOException ex) {
                Logger.getLogger(PowerShellScript.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShellScript.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        private static final long serialVersionUID = 1L;
    }

    @Extension
    public static final class DescriptorImpl extends DurableTaskDescriptor {

        @Override
        public String getDisplayName() {
            return "PowerShell";
        }

    }

}
