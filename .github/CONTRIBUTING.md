**:tada: Thanks you for taking the time to contribute to this repository! :tada:**

Issues can be created for bugs :bug:, feature suggestions :heavy_plus_sign:, performance issues :snail:, questions :question: or anything else.
All issues should follow the provided template when creating a new issue.

Before opening new issues, make sure that the issue does not already exist, so search through all issues (even the closed ones) before opening a new issue.

### Performance issues :snail:

If you encounter a performance issue (i.e. server/client lag), make sure to read the following.

You can use the [Sampler mod](https://forum.industrial-craft.net/thread/10820) for _profiling_ your game.
This mod can create `.nps` files, and can help us to see what parts of the game cause performance issues using software such as [VisualVM](https://visualvm.github.io/).
When opening a performance issue, make sure to send us this `.nps` file.

If you are encountering client lag (FPS issues), run this mod in your client.
If you are encountering server lag (TPS issues), run this mod on your server.

When the lag is starting, run `/sampler start` to start profiling, and `/sampler stop` to stop.
Run `/sampler export [your file name]` to export this profiling result to an `.nps` file.

Download Sampler for Minecraft 1.12: http://files.player.to/sampler-1.73.jar

### Pull Request

Pull requests are more than welcome! Before submitting one, make sure to discuss your plans with the maintainers via an issue.
When you submit PRs, you have to sign the [Cyclops Contributor License Agreement](https://cla-assistant.io/CyclopsMC/CyclopsCore),
which makes sure that you give us the appropriate permissions to use your submission, and that your submission is not owned by someone else.
