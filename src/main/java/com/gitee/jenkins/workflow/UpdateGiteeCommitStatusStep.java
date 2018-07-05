package com.gitee.jenkins.workflow;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.export.ExportedBean;

import com.gitee.jenkins.gitee.api.model.BuildState;
import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;

/**
 * @author <a href="mailto:robin.mueller@1und1.de">Robin Müller</a>
 */
@ExportedBean
public class UpdateGiteeCommitStatusStep extends Step {

    private String name;
    private BuildState state;

    @DataBoundConstructor
    public UpdateGiteeCommitStatusStep(String name, BuildState state) {
        this.name = StringUtils.isEmpty(name) ? null : name;
        this.state = state;
    }
    
	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new UpdateGiteeCommitStatusStepExecution(context, this);
	}

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = StringUtils.isEmpty(name) ? null : name;
    }

    public BuildState getState() {
        return state;
    }

    @DataBoundSetter
    public void setState(BuildState state) {
        this.state = state;
    }

    public static class UpdateGiteeCommitStatusStepExecution extends AbstractSynchronousStepExecution<Void> {
        private static final long serialVersionUID = 1;

        private final transient Run<?, ?> run;

        private final transient UpdateGiteeCommitStatusStep step;

        UpdateGiteeCommitStatusStepExecution(StepContext context, UpdateGiteeCommitStatusStep step) throws Exception {
            super(context);
            this.step = step;
            run = context.get(Run.class);
        }
        
        @Override
        protected Void run() throws Exception {
            final String name = StringUtils.isEmpty(step.name) ? "jenkins" : step.name;
            PendingBuildsAction action = run.getAction(PendingBuildsAction.class);
            if (action != null) {
                action.startBuild(name);
            }
            return null;
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getDisplayName() {
            return "Update the commit status in Gitee";
        }

        @Override
        public String getFunctionName() {
            return "updateGiteeCommitStatus";
        }

        public ListBoxModel doFillStateItems() {
            ListBoxModel options = new ListBoxModel();
            for (BuildState buildState : EnumSet.allOf(BuildState.class)) {
                options.add(buildState.name());
            }
            return options;
        }

		@Override
		public Set<Class<?>> getRequiredContext() {
			return ImmutableSet.of(TaskListener.class, Run.class);
		}
    }
}