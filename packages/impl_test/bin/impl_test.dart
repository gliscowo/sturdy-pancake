import 'dart:async';
import 'dart:ffi';
import 'dart:isolate';

import 'package:impl_test/bindings.dart';
import 'package:sturdy_pancake_bindings/sturdy_pancake_bindings.dart';

int main() {
  setupBindingsResolver(['package:impl_test/bindings.dart']);

  // Isolate.run(() async {
  //   while (true) {
  //     await Future.delayed(const Duration(seconds: 1));
  //     print(DateTime.now());
  //   }
  // });

  for (var i = 0; i < 100; i++) {
    print('[$i] game profile name: ${Minecraft.getInstance().gameProfile.name}');
  }

  // ClientSendMessageEvents.chat.register(marshallAs(
  //   r'net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents$Chat',
  //   (message) => print('chat message: $message')
  // ));

  return 0;
}

