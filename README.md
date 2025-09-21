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

### `.autocorrect [enable/disable]`
Enables or disables autocorrect for phrases.
- Autocorrect uses **Levenshtein distance** to match similar phrases.

---

### `.set <phrase> <count>`
Sets the counter of a phrase to the specified value.
- The phrase must be **at least 3 letters long**.
- The count must be **≥ 0**.

---

### `.append <phrase>`
Appends a tracked phrase to the end of each sentence.

- Running the command without arguments removes the append phrase.
- The append is **cancelled** if:
    - The phrase is already at the start or end of the sentence.
    - The sentence contains only punctuation.
    - The sentence is inside `()`, `[]`, or `{}`.

---

### `.distance <value>`
Shows or sets the maximum Levenshtein distance used for autocorrect.

---

### `.shortcut add <phrase> <shortcut>`
Adds a shortcut for a phrase.
- Shortcuts will be replaced with their corresponding phrase.

---

### `.shortcut remove <shortcut>`
Removes a shortcut.

---

## Example Usage
```plaintext
.track :thumbsup:
.track RIP
.set :thumbsup: 10
.append :thumbsup:
.autocorrect enable
.shortcut add :thumbsup: tu
