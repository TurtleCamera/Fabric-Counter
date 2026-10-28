# Counter Mod

This is a personal Minecraft client-side mod that tracks counters for various phrases (such as `:thumbsup:`, `RIP`, etc.) and can also automate using these phrases and counters in chat.

---

## Commands

> **Command syntax:**
> - All **base commands** (the ones starting with `.`) are **literals**.
> - Everything in `[ ]` is also a **literal**.
> - Everything in `< >` is a **non-literal argument** (something you provide, like a phrase or number).

### `.help`
Displays the list of available commands and their descriptions.

---

### `.track <phrase>`
Starts counting the number of times a phrase is used for each server.
- The phrase must be **at least 3 letters long**.

---

### `.untrack <phrase>`
Stops counting the specified phrase.
- The phrase must be **at least 3 letters long**.

---

### `.list [phrase/shortcut]`
Lists the currently tracked phrases or shortcuts.
- If an argument is provided, lists all phrases or shortcuts.

---

### `.enable [true/false]`
Enables or disables automatic counting for phrases.

---

### `.set <phrase> <count>`
Sets the counter of a phrase to the specified value.
- The phrase must be **at least 3 letters long**.
- The count must be **≥ 0**.

---

### `.append <phrase>`
Appends the specified phrase to the end of each sentence.
- The phrase does **not** need to be tracked.
- Tracked phrases receive their counter suffix; untracked phrases are appended as plain text.
- Running the command without arguments removes the append phrase.
- The append is **cancelled** if:
  - **Tracked append phrase:** Any tracked phrase is already at the start or end of the sentence.
  - **Untracked append phrase:** The assigned append phrase is already at the start or end of the sentence. Other tracked phrases do not affect this check.
  - The sentence contains only punctuation.
  - The sentence is inside `()`, `[]`, or `{}`.

---

### `.bindphrase [phrase/clear]`
Assigns a phrase to the **Send Assigned Phrase** keybind.
- `.bindphrase <phrase>` assigns the phrase, whether tracked or untracked.
- Typing `.bindphrase` without an argument removes the assignment.

---

### `.shortcut add <phrase> <shortcut>`
Adds a shortcut for a phrase.
- Shortcuts will be replaced with their corresponding phrase.

---

### `.shortcut remove <shortcut>`
Removes a shortcut.

---

## Keybinds

Both keybinds appear under **Phrase Counter** in Minecraft's **Controls → Key Binds** menu and can be reassigned.

| Keybind | Default | Action |
| --- | --- | --- |
| **Send Assigned Phrase** | **R** | Sends the phrase assigned with `.bindphrase` without opening chat. |
| **Batch Phrases Modifier** | **Left Shift** | Hold while pressing the phrase key to queue messages; release to send them together. |

- Each press while the batch modifier is held adds another entry. For example, three presses can produce `RIP X1, RIP X2, and RIP X3` in one chat message.
- Tracked phrases receive an incremented counter suffix when counting is enabled; untracked phrases are sent as plain text.
- When `.enable false` is set, the keybind still sends the assigned phrase, but without a counter suffix.
- If the resulting message exceeds Minecraft's **256-character** chat limit, it is not sent and **no counters are incremented**.

---

## Example Usage
```plaintext
.track :thumbsup:
.track RIP
.set :thumbsup: 10
.append :thumbsup:
.enable true
.shortcut add :thumbsup: tu
.bindphrase RIP
