VAYU — Autonomous Android AI Agent
What It Is
VAYU is a fully autonomous AI agent that controls an Android phone the same way a human does — by looking at the screen and tapping, typing, swiping. No app APIs, no shortcuts. Pure screen interaction.
Architecture

┌─────────────────────────────────────┐
│         Android Phone               │
│                                     │
│  ┌──────────────┐  ┌─────────────┐  │
│  │ VAYU App     │  │   Termux    │  │
│  │ (APK)        │  │  brain.py   │  │
│  │              │◄─►  Flask      │  │
│  │ VayuService  │  │  :8082      │  │
│  │ MainActivity │  └──────┬──────┘  │
│  └──────────────┘         │         │
└───────────────────────────┼─────────┘
                            │ HTTPS
                    ┌───────▼────────┐
                    │  Gemini 2.0    │
                    │  Flash API     │
                    │  (Google)      │
                    └────────────────┘


Components
1. VayuService.kt (The Hands)
An Android Accessibility Service — the only way to control a phone programmatically without root
Captures the screen every step using takeScreenshot() (API 30+)
Reads every visible UI element via AccessibilityNodeInfo tree (all text, buttons, inputs, their coordinates)
Executes actions: TAP, LONG_PRESS, SWIPE, TYPE, SCROLL, OPEN_APP, PRESS_BACK, PRESS_HOME
Runs a ReAct loop — up to 30 steps per task, each step: screenshot → brain → action → repeat
Polls the brain every 2 seconds for new tasks (can work 24/7 even when you're asleep)
2. brain_termux.py (The Brain)
Flask server running locally on Termux at localhost:8082
Receives: goal + screenshot (base64 JPEG) + UI tree (JSON) + action history
Sends everything to Gemini 2.0 Flash via direct REST API
Gemini looks at the screen like a human and decides the next action
Returns a structured JSON action back to VayuService
Has a task queue — you can submit tasks remotely via POST /task/submit
3. MainActivity.kt (The Face)
Simple dark UI to monitor agent status and submit tasks manually
Shows live service status, current running task
Detects if Accessibility Service is enabled
How a Task Executes


You submit: "Open YouTube and search for lo-fi music"
        ↓
VayuService picks it up from queue
        ↓
Step 1: Screenshot → UI tree → send to brain
Brain: {"action": "OPEN_APP", "package": "com.google.android.youtube"}
        ↓
Step 2: YouTube opens → new screenshot → send to brain
Brain: {"action": "TAP", "x": 950, "y": 72}  ← search icon
        ↓
Step 3: Search bar focused → brain types
Brain: {"action": "TYPE", "text": "lo-fi music"}
        ↓
Step 4: Brain sees results
Brain: {"action": "DONE", "reason": "Search completed successfully"}


Key Design Decisions
Decision
Reason
Accessibility Service
No root needed, works on any Android 10+
Brain on Termux (localhost)
Zero network latency, no cloud dependency for the server
Gemini 2.0 Flash
Fast, free tier, multimodal (sees screenshots)
Direct REST API (no SDK)
SDKs have Rust/pydantic deps that break on Termux
Screenshot + UI tree together
Screenshot for visual context, UI tree for precise coordinates
ReAct loop (max 30 steps)
Prevents infinite loops, handles multi-step tasks
What It Can Do
Anything a human can do on the phone:
Browse and interact with any app
Send messages, emails
Search the web
Post on social media
Fill forms
Watch/like YouTube videos
Book things, order things
Run autonomous content pipelines while you sleep


VAYU — Autonomous Android AI Agent
What It Is
VAYU is a fully autonomous AI agent that controls an Android phone the same way a human does — by looking at the screen and tapping, typing, swiping. No app APIs, no shortcuts. Pure screen interaction.
Architecture

## Run Locally

**Prerequisites:**  Node.js


1. Install dependencies:
   `npm install`
2. Set the `GEMINI_API_KEY` in [.env.local](.env.local) to your Gemini API key
3. Run the app:
   `npm run dev`
