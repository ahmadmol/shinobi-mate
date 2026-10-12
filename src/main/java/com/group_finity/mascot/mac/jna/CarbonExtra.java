package com.group_finity.mascot.mac.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface CarbonExtra extends Library {
   CarbonExtra INSTANCE = (CarbonExtra)Native.load("Carbon", CarbonExtra.class);
   NativeLibrary nl = NativeLibrary.getProcess();
   Pointer kCurrentUser = nl.getGlobalVariableAddress("kCFPreferencesCurrentUser").getPointer(0L);
   Pointer kAnyHost = nl.getGlobalVariableAddress("kCFPreferencesAnyHost").getPointer(0L);
   long kAXErrorSuccess = 0L;
   long kAXValueCGPointType = 1L;
   long kAXValueCGSizeType = 2L;
   long kCFNumberInt32Type = 3L;

   long GetFrontProcess(ProcessSerialNumber var1);

   long GetProcessPID(ProcessSerialNumber var1, LongByReference var2);

   long AXUIElementCopyAttributeValue(AXUIElementRef var1, CoreFoundation.CFStringRef var2, PointerByReference var3);

   int AXUIElementSetAttributeValue(AXUIElementRef var1, CoreFoundation.CFStringRef var2, CoreFoundation.CFTypeRef var3);

   AXUIElementRef AXUIElementCreateApplication(long var1);

   boolean AXValueGetValue(AXValueRef var1, long var2, Pointer var4);

   AXValueRef AXValueCreate(long var1, Pointer var3);

   CoreFoundation.CFTypeRef CFPreferencesCopyValue(CoreFoundation.CFStringRef var1, CoreFoundation.CFStringRef var2, Pointer var3, Pointer var4);

   boolean CFPreferencesAppSynchronize(CoreFoundation.CFStringRef var1);

   int CFStringGetSystemEncoding();

   void CFShow(CoreFoundation.CFTypeRef var1);
}
