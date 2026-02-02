// ignore_for_file: unused_element

part of '../sturdy_pancake_bindings.dart';

extension type Logger._(JavaObject _) {
  @pragma('vm:external-name', '::org.slf4j.Logger::info')
  external void info(String message);
}

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::getLogger')
external Logger getLogger(String name);

final _printLogger = getLogger("dart");

@entryPoint
void _printToLogger(String line) => _printLogger.info(line);
