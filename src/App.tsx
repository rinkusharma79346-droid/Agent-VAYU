/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { FileCode2, Smartphone, Terminal } from "lucide-react";

export default function App() {
  return (
    <div className="min-h-screen bg-[#0a0a0b] text-[#ffffff] font-sans flex flex-col overflow-hidden">
      <header className="h-16 border-b border-[#262629] flex items-center justify-between px-6 shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-6 h-6 bg-[#00ff66] rounded flex items-center justify-center text-[#0a0a0b] font-bold text-sm">V</div>
          <div>
            <h1 className="text-[18px] tracking-[-0.5px] font-bold leading-tight">VAYU</h1>
            <p className="text-[10px] text-[#88888b] leading-tight">Autonomous Android AI Agent</p>
          </div>
        </div>
        <div className="flex gap-4">
          <div className="px-2.5 py-1 rounded text-[11px] font-semibold uppercase border border-[#00ff66] text-[#00ff66] tracking-wider">
            System: Online
          </div>
        </div>
      </header>

      <main className="flex-1 p-6 max-w-5xl mx-auto w-full flex flex-col gap-6 overflow-y-auto">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Android Client Card */}
          <div className="bg-[#141416] border border-[#262629] flex flex-col">
            <div className="p-4 text-[11px] text-[#88888b] uppercase tracking-[0.1em] border-b border-[#262629] flex items-center gap-2">
              <Smartphone className="w-4 h-4 text-[#00ff66]" />
              Android Client
            </div>
            <div className="p-4">
              <p className="text-[13px] text-[#88888b] mb-4">
                The Android project files have been generated in the <code className="font-mono text-[#00ff66] bg-[#1a1a1c] px-1 py-0.5 rounded border border-[#262629]">V-claw/</code> directory.
              </p>
              <ul className="text-[13px] space-y-3 text-[#ffffff]">
                <li className="flex items-center gap-2 border-b border-[#262629] pb-2 last:border-0 last:pb-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00ff66]"></span>
                  Accessibility Service (VayuService.kt)
                </li>
                <li className="flex items-center gap-2 border-b border-[#262629] pb-2 last:border-0 last:pb-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00ff66]"></span>
                  UI Dashboard (MainActivity.kt)
                </li>
                <li className="flex items-center gap-2 border-b border-[#262629] pb-2 last:border-0 last:pb-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00ff66]"></span>
                  Gradle Build Configuration
                </li>
              </ul>
            </div>
          </div>

          {/* Termux Brain Card */}
          <div className="bg-[#141416] border border-[#262629] flex flex-col">
            <div className="p-4 text-[11px] text-[#88888b] uppercase tracking-[0.1em] border-b border-[#262629] flex items-center gap-2">
              <Terminal className="w-4 h-4 text-[#00ff66]" />
              Termux Brain
            </div>
            <div className="p-4">
              <p className="text-[13px] text-[#88888b] mb-4">
                The Python Flask server that connects to Gemini 2.0 Flash is ready.
              </p>
              <ul className="text-[13px] space-y-3 text-[#ffffff]">
                <li className="flex items-center gap-2 border-b border-[#262629] pb-2 last:border-0 last:pb-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00ff66]"></span>
                  <code className="font-mono text-[#00ff66] bg-[#1a1a1c] px-1 py-0.5 rounded border border-[#262629]">brain_termux.py</code>
                </li>
                <li className="flex items-center gap-2 border-b border-[#262629] pb-2 last:border-0 last:pb-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00ff66]"></span>
                  ReAct Loop logic
                </li>
                <li className="flex items-center gap-2 border-b border-[#262629] pb-2 last:border-0 last:pb-0">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00ff66]"></span>
                  Direct REST API integration
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* How to use Card */}
        <div className="bg-[#141416] border border-[#262629] flex flex-col">
          <div className="p-4 text-[11px] text-[#88888b] uppercase tracking-[0.1em] border-b border-[#262629] flex items-center gap-2">
            <FileCode2 className="w-4 h-4 text-[#00ff66]" />
            How to use
          </div>
          <div className="p-4">
            <ol className="text-[13px] text-[#88888b] space-y-3 list-decimal list-inside font-mono">
              <li className="hover:bg-[#1a1a1c] p-2 rounded transition-colors border-l-2 border-transparent hover:border-[#00ff66]">Download the project files from the workspace explorer.</li>
              <li className="hover:bg-[#1a1a1c] p-2 rounded transition-colors border-l-2 border-transparent hover:border-[#00ff66]">Open the <code className="text-[#ffffff]">V-claw</code> folder in Android Studio to build the APK.</li>
              <li className="hover:bg-[#1a1a1c] p-2 rounded transition-colors border-l-2 border-transparent hover:border-[#00ff66]">Install the APK on your Android device (Android 10+).</li>
              <li className="hover:bg-[#1a1a1c] p-2 rounded transition-colors border-l-2 border-transparent hover:border-[#00ff66]">Install Termux on your device and run <code className="text-[#ffffff]">python brain_termux.py</code>.</li>
              <li className="hover:bg-[#1a1a1c] p-2 rounded transition-colors border-l-2 border-transparent hover:border-[#00ff66]">Enable the Accessibility Service in Android Settings.</li>
              <li className="hover:bg-[#1a1a1c] p-2 rounded transition-colors border-l-2 border-transparent hover:border-[#00ff66]">Submit tasks via the App UI or the Termux REST API.</li>
            </ol>
          </div>
        </div>
      </main>

      <footer className="h-10 border-t border-[#262629] bg-[#0d0d0f] flex items-center justify-between px-6 font-mono text-[10px] text-[#88888b] shrink-0">
        <div>ROOT: ~/vayu-android-agent/app/src/main/</div>
        <div className="flex gap-4">
          <span>STATUS: <b className="text-[#ffffff]">IDLE</b></span>
          <span>API: <b className="text-[#ffffff]">0/1500</b></span>
        </div>
      </footer>
    </div>
  );
}
