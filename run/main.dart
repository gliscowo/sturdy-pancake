import 'bindings.dart';

int main() {
  print('game profile: ${Minecraft.getInstance().getGameProfile().name}');
  return 0;
}