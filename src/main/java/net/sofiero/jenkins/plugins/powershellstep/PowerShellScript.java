package net.sofiero.jenkins.plugins.powershellstep;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.IOException;
import org.jenkinsci.plugins.durabletask.DurableTaskDescriptor;
import org.jenkinsci.plugins.durabletask.FileMonitoringTask;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Runs a PowerShell script.
 */
public final class PowerShellScript extends FileMonitoringTask {
    private final String script;

    @DataBoundConstructor public PowerShellScript(String script) {
        this.script = script;
    }
    
    public String getScript() {
        return script;
    }

    @Override protected FileMonitoringTask.FileMonitoringController doLaunch(FilePath ws, Launcher launcher, TaskListener listener, EnvVars envVars) throws IOException, InterruptedException {
        if (launcher.isUnix()) {
            throw new IOException("PowerShell scripts can only be run on Windows nodes");
        }
        BatchController c = new BatchController(ws);

        c.getBatchFile(ws).write(script, "UTF-8");

        Launcher.ProcStarter ps = launcher.launch().cmds("powershell.exe", "&", String.format("'%s'", c.getBatchFile(ws).getRemote(), ">", c.getLogFile(ws), "2>", c.getResultFile(ws))).envs(envVars).pwd(ws);        
        try {
            Launcher.ProcStarter.class.getMethod("quiet", boolean.class).invoke(ps, true); // TODO 1.576+ remove reflection
            listener.getLogger().println("[" + ws.getRemote().replaceFirst("^.+\\\\", "") + "] Running PowerShell script"); // details printed by cmd
        } catch (NoSuchMethodException x) {
            // older Jenkins, OK
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

        public FilePath getBatchFile(FilePath ws) {
            return controlDir(ws).child("jenkins-main.ps1");
        }

        private static final long serialVersionUID = 1L;
    }

    @Extension public static final class DescriptorImpl extends DurableTaskDescriptor {

        @Override public String getDisplayName() {
            return "PowerShell";
        }

    }

}
