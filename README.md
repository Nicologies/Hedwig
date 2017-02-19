# Hedwig

A configurable TeamCity plugin that notifies your [Slack](https://slack.com) or [Hipchat](https://www.hipchat.com/) recipients.

With this plugin you will be notified if a build fails and if you 

- are the author, assignee, or you've commented on a pull request being built
- triggerred the build

Additionally, you can also send out message using Teamcity's Service Message facility.

## Installation

Install [PrExtras](https://github.com/Nicologies/PrExtras) and [UserMapping](https://github.com/Nicologies/usermapping)

Download the [plugin](https://github.com/Nicologies/Hedwig/releases/latest).

Follow the TeamCity [plugin installation directions](http://confluence.jetbrains.com/display/TCD8/Installing+Additional+Plugins).

## Configuration

### Slack

Create an [incoming webook](https://my.slack.com/services/new/incoming-webhook) in Slack.

Pick a Slack channel as required by Slack, but we won't actually send any message to this channel, instead, we will send the message directly to the user.

Pick a Slack bot name as required by Slack.

Copy the URL for the webhook. As an admin, create a teamcity parameter `system.hedwig.slack.webhook_url` with the value of the Slack webhook url.
Create another parameter `system.hedwig.slack.bot_name` with the bot name you have chosen.

### Hipchat

- Create a hipchat [personal token](https://helixleisure.hipchat.com/account/api) with the permissions of `Send Message` and `Send Notification`.
- Define a teamcity parameter `system.hedwig.hipchat.token` with the token generated above.

### User Mapping

You may want to configure  [UserMapping](https://github.com/Nicologies/usermapping) if your GitHub/TeamCity username is different to the slack username.

## Sendout a service message

```
"##teamcity[Hedwig Status='Succeeded' StatusType='Succeeded' MsgName0='DropFolder' MsgValue0='%Embed.DropFolder%' PrAuthor='%teamcity.build.pull_req.author%' PrAssignee='%teamcity.build.pull_req.assignee%' PrUrl='%teamcity.build.pull_req.url%' Branch='%teamcity.build.pull_req.branch_name%' Users='%teamcity.build.triggered_by.mapped_user%;%teamcity.build.pull_req.participants%' Channels='room1;room2;room3']"
```

You can add a lot of messages (<50 I guess) as you want, just name it as `MsgNameX='The name', MsgValueX='The value'` where `X` is a number.
