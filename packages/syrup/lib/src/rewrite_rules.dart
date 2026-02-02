import 'package:syrup/syrup.dart';

typedef RewriteRule<T> =
    void Function(Object? json, Location location, MutatorNode<T> node, void Function(Object? json) replace);

RewriteRule forType<T>(RewriteRule<T> inner) => (json, location, node, replace) {
  if (node is! MutatorNode<T>) {
    return;
  }

  inner(json, location, node, replace);
};

RewriteRule<T> atIndex<T>(int index, RewriteRule<T> inner) => (json, location, node, replace) {
  if (location.segments.last case IndexLocationSegment segment when segment.index == index) {
    inner(json, location, node, replace);
  }
};

RewriteRule<T> atKey<T>(String key, RewriteRule<T> inner) => (json, location, node, replace) {
  if (location.segments.last case KeyLocationSegment segment when segment.key == key) {
    inner(json, location, node, replace);
  }
};

RewriteRule<T> remap<T>(T input, T replacement) => (json, location, node, replace) {
  if (node.decode(json) == input) {
    replace(node.encode(replacement));
  }
};

RewriteRule<T> overwrite<T>(T replacement) => (json, location, node, replace) {
  replace(node.encode(replacement));
};

RewriteRule sequential(List<RewriteRule> mutators) => (json, location, node, replace) {
  for (final mutator in mutators) {
    mutator(json, location, node, replace);
  }
};
