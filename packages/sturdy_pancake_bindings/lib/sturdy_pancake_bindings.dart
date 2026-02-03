import 'dart:developer';
import 'dart:ffi';
import 'dart:isolate';

import 'package:syrup/syrup.dart';
import 'package:vm_service/vm_service.dart' hide Isolate;
import 'package:vm_service/vm_service_io.dart';

// we build the bindings library with part instead
// of export directives because we otherwise have
// a much harder time calling into entrypoints
// when they're spread across files

part 'src/core.dart';
part 'src/hot_reload.dart';
part 'src/logging.dart';
part 'src/recipe.dart';
