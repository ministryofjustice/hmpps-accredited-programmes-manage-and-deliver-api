# Adding or removing a developer from the Deployment Slack Notification Workflow

This document explains what you need to do to add (or remove) a developer to Manage & Deliver's Slack Integration bot to alert developers about Deployments that require their

## Some context

The Manage and Deliver team have an automated step in our GitHub Actions have a "deploy gate" feature in our CI, which requires a manual developer intervention before the Production environment is deployed.

This allows a developer to do any last-minute checks in the Development environment, before releasing to Production.

This is configures in the `.github/workflows/pipeline.yml` file in the `notify_for_prod_approval` job.

To work correctly, the pipeline links three bits of information:

One: What is the github username of the author of the PR?  This comes for free in the GitHub action.

Two: How can we identify that person on Slack?  We do this with a simple key-value hash map in JSON, where the key is the GitHub username, and the value is the "member ID" on Slack.  We store this in the `GH_TO_SLACK_MAPPING` Repository variable in GitHub (more on that below).  An example value looks like:

```json
{
    "alice-github-username": "D0000ABCDE"
}
```

Three: What URL should we ping with the information?  This is stored in the `SLACK_WEBHOOK_URL` Repository _secret_ (not variable).  You won't need to change this value at all, but it's useful to know.

## Adding or Removing the value

You'll need to do this in two steps:

1. Get the Developer's details
2. Update the Repo values

### Getting the Developer's details

Use the same github username as the one in the `hmpps-github-teams` used to assign the Developer to the various teams

Fetch your slack ID:

1. In Slack's UI, click on your own profile picture
2. View your Profile
3. Hit the elipses (...) after "Edit status" and "View as"
4. Click "Copy member ID"

### Update the repository's values 

1. Go to the repo in the github web UI
2. Click on the repository settings
3. In the left-hand menu go to Security > Secrets and variables > Actions
4. Click the "Variables" tab at the top (a sibling to the "Secrets" tab)
5. Find the "Repository variables" section
6. Find the `GH_TO_SLACK_MAPPING` variable in that table
6. Update the variable with your github username as the key, and the Slack member ID as the value.

You are editing inline JSON, to be careful with quotation marks and commas.  You might find it helpful to copy-paste the value into a code editor.


