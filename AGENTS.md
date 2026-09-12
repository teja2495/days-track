# Validation

- Do not run automated UI tests unless requested. Manual UI testing is performed by the user.
- After every completed coding task, always run this exact command from the repository root outside the sandbox before considering the task complete:

```bash
GRADLE_USER_HOME=$PWD/.gradle-codex ./gradlew assembleDebug && adb install --user 0 -r app/build/outputs/apk/debug/app-debug.apk && adb shell am force-stop com.tk.daystrack && adb shell am start -W -n com.tk.daystrack/.MainActivity
```

- If any part fails, diagnose it and rerun the full command until it succeeds. If no device is connected or the environment is blocked, report that boundary plainly instead of claiming the task is complete.

# Git

- Do not run `git add` or `git commit` without the user's permission.
