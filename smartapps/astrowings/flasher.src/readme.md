# Flasher

### Flasher App
This SmartApp flashes selected lights when an alarm is triggered.

### Installation Notes
* Make sure whichever siren you choose to use to trigger the flasher is also selected in the 'Alert with sirens' section of your SHM configuration.
That way, when an intrusion is detected by the SHM, it will trigger the siren which in turn will activate the flasher. DO NOT configure the same
lights to turn on with the 'Alert with lights' section of the SHM configuration as this will interfere with the operation of the flasher.

## Software Development
### Change Log
* See "Version History" commented section of the .groovy file

### Upcoming changes, requests, todo list
* Try different flash method to get around having to schedule separate 15-second flash sequences (explanation below).

### Known Issues
* Depending on the particular devices used to switch the lights, flash on/off times that are too short may result in erratic behaviour where the light will seem to skip one or more commands. I don't think there's any way around this and that it's just normal because of the time it takes to process the command. I personally use an on/off cycle of 1000ms/400ms which seems to work pretty well besides the occasional skip.
* Smartapp execution is limited to 20 seconds. Because of this limitation, this app limits each flash sequence duration to remain below that threshold (approx. 15 sec to be safe), and achieves desired flash duration (e.g. 2 minutes) by scheduling successive flash cycles. Each flash sequence is spaced apart by a few seconds to make sure it doesn't get skipped, so that's why you'll observe a small pause every 15 seconds or so through the flash duration. I'll experiment with a different method of flashing the light to get around this limitation in a future release.

## About the Author
Currently living in Alberta, Canada, I've been serving in the Canadian Air Force since 1998 and I am by no means an advanced programmer. I do however like to learn and come up with my own solutions to problems. I have absolutely no formal training/education in software development, but I like to learn new things and find ways to put them to good use. I use Excel quite a lot, both for work and personal data organization, I have some experience with MS Access, and a basic knowledge at writing VBA code to suit my needs.<br><br>

I acquired a SmartThings hub in late summer of 2016 and have taken an interest in learning to write my own applications. I still have a lot of learning to do and my code isn't perfect, but it works for me and I can usually find a way to get it to do what I want. That being said, I'm open to comments and suggestions on how to improve my coding method, and will also consider requests if you need one of my apps tweaked for your particular use.<br><br>

<img src="https://raw.githubusercontent.com/astrowings/SmartThings/master/images/clown.JPG" width="80" height="80" align="left">
Thanks for stopping by to read about me.<br>
@astrowings
<br><br><br>
I don't want to post a link to my email address, but if you want to contact me you can do so by emailing the author at Google's email service :wink:. To report a bug or submit a suggestion regarding this SmartApp, please use the 'GitHub Project Issues Link' below.

## Contributors
Nobody else contributed directly to the development of this SmartApp, but I did learn a lot from comments, suggestions, and code snippets posted in the [SmartThings Community](https://community.smartthings.com/) discussion topics. Thanks to all of those who contribute their time, expertise, and share their knowledge to help others.

## Links and How-To's
#### [GitHub Project Issues Link](https://github.com/astrowings/SmartThings/issues)
* Use this link to report a bug or submit a suggestion for improving this SmartApp. Since all my SmartApps are part of the same GitHub 'Project', __be sure to specify which SmartApp your issue relates to__.

#### [SmartThings IDE GitHub Integration Instructions](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html)
* This document explains how to link GitHub to your SmartThings IDE. This allows you to be able to see when this SmartApp gets updated and enables you to apply the updates much more easily. _"The GitHub IDE integration allows you to integrate your forked SmartThingsPublic repository with the IDE. This allows you to easily view and work with SmartApps or Device Handlers already in the repository, as well as update the versions in your IDE with upstream repository changes, and make commits to your forked repository right from the IDE."_
