// ignore_for_file: unused_element

part of '../sturdy_pancake_bindings.dart';

VmService? _vmServiceInstance;
Logger? _vmServiceLogger;

Future<void> _setupVmService() async {
  final uri = (await Service.getInfo()).serverWebSocketUri!;
  _vmServiceInstance = await vmServiceConnectUri(uri.toString());
  _vmServiceLogger = getLogger('dart-embedder');

  await _vmServiceInstance!.streamListen(EventStreams.kIsolate);
  _vmServiceInstance!.onIsolateEvent.listen((event) {
    if (event.kind != 'IsolateReload') return;

    _vmServiceLogger!.info('hot reload completed');
    _notifyReloadComplete();
  });
}

@entryPoint
void _requestHotReload() async {
  if (_vmServiceInstance == null) {
    await _setupVmService();
  }

  _vmServiceLogger!.info('requesting hot reload');
  _vmServiceInstance!.reloadSources(Service.getIsolateId(Isolate.current)!);
}

@pragma('vm:external-name', '::io.wispforest.sturdypancake.NativeCallbacks::notifyReloadComplete')
external void _notifyReloadComplete();
