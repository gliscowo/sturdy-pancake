import 'dart:isolate';

import 'package:impl_test/bindings.dart';
import 'package:sturdy_pancake_bindings/sturdy_pancake_bindings.dart';

void main() {
  setupBindingsResolver(['package:impl_test/bindings.dart']);

  Isolate.run(() async {
    await Future.delayed(const Duration(seconds: 5));
    print(Minecraft.getInstance().gameProfile.name);
  });

  ClientSendMessageEvents.chat.register(
    (message) {
      print('chat message: $message');
    }.marshallAs(r'net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents$Chat'),
  );
}
