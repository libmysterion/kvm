package com.mystery.kvm.common.transparentwindow;

import com.mystery.libmystery.bytes.ByteFunctions;
import com.mystery.libmystery.nio.Callback;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;


public class KeyHook {

    private static HHOOK hhk;
    private static LowLevelKeyboardProc keyboardHook;
    private static User32 lib;

    public static void blockWindowsKey(Callback<Integer> pressedFn, Callback<Integer> releasedFn) {
        if (isWindows()) {
            new Thread(new Runnable() {

                int c = 0;

                @Override
                public void run() {
                    lib = User32.INSTANCE;
                    HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
                    keyboardHook = new LowLevelKeyboardProc() {
                        public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
                            if (nCode >= 0) {

                                int TRANSITION_STATE_BIT = 7;
                                int TRANSITION_STATE = ByteFunctions.longToBytes((long) info.flags)[TRANSITION_STATE_BIT];
                                boolean pressed = TRANSITION_STATE == 0;

                                switch (info.vkCode) {
                                    case 0x5B:  // left windows key
                                    case 0x5C: //right windows key
                                    {

                                        if (pressed) {
                                            pressedFn.onSuccess(524);

                                            System.out.println("keypressed :" + info.vkCode);
                                        } else {

                                            // windows key is never pressed...only released...
                                            // AND IT GETS RELEASED TWICE..ONCE FOR PRESS ONCE FOR RELEASE
                                            if (c++ % 2 == 0) {
                                                pressedFn.onSuccess(524);
                                            }
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException ex) {
                                                ex.printStackTrace();

                                            }
                                            releasedFn.onSuccess(524);

                                            System.out.println("key_released :" + info.vkCode);
                                        }
                                        return new LRESULT(1);
                                    }

                                    default: //do nothing
                                    }
                            }
                            return lib.CallNextHookEx(hhk, nCode, wParam, info.getPointer());
                        }
                    };
                    hhk = lib.SetWindowsHookEx(13, keyboardHook, hMod, 0);

                    // This bit never returns from GetMessage
                    int result;
                    MSG msg = new MSG();
                    while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
                        if (result == -1) {
                            break;
                        } else {
                            lib.TranslateMessage(msg);
                            lib.DispatchMessage(msg);
                        }
                    }
                    lib.UnhookWindowsHookEx(hhk);
                }
            }).start();
        }
    }

    public static void unblockWindowsKey() {
        if (isWindows() && lib != null) {
            lib.UnhookWindowsHookEx(hhk);
        }
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }
}
