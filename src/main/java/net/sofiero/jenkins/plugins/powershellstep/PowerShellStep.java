/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sofiero.jenkins.plugins.powershellstep;

import hudson.Extension;
import org.jenkinsci.plugins.durabletask.DurableTask;
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author mad
 */
public class PowerShellStep extends DurableTaskStep {

    private final String script;

    @DataBoundConstructor
    public PowerShellStep(String script) {
        if (script == null) {
            throw new IllegalArgumentException();
        }
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    @Override
    protected DurableTask task() {
        return new PowerShellScript(script);
    }

    @Extension
    public static final class DescriptorImpl extends DurableTaskStepDescriptor {

        @Override
        public String getDisplayName() {
            return "Windows PowerShell Script";
        }

        @Override
        public String getFunctionName() {
            return "powershell";
        }

    }

}
