# lein-test-reload-bug

Reproduces `lein test` reloading namespaces too often.

This bug stems from the fact that
```clojure
  (require :reload 'A 'B)
```
will load B twice if A depends on B.

This repository attempts to reproduce this reliably to demonstrate
that `lein test`'s use of require :reload [here](https://github.com/technomancy/leiningen/blob/df7122f6aa5599f515b66522ade87c92777b25f2/src/leiningen/test.clj#L86) is problematic.

## Usage

See `lein-test-reload-bug.core-test` for explanation.

```clojure
~/Projects/lein-test-reload-bug master !6 ❯ lein test                                                                                                    4s vim
OpenJDK 64-Bit Server VM warning: Options -Xverify:none and -noverify were deprecated in JDK 13 and will likely be removed in a future release.
"loading" lein-test-reload-bug.a-deftype
"loading" lein-test-reload-bug.b-protocol
"loading" lein-test-reload-bug.a-protocol
"loading" lein-test-reload-bug.b-deftype
"loading" lein-test-reload-bug.b-protocol
"loading" lein-test-reload-bug.core-test

lein test lein-test-reload-bug.core-test

lein test :only lein-test-reload-bug.core-test/a-test

ERROR in (a-test) (core_deftype.clj:583)
Uncaught exception, not in assertion.
expected: nil
  actual: java.lang.IllegalArgumentException: No implementation of method: :b of protocol: #'lein-test-reload-bug.b-protocol/B found for class: lein_test_reload
_bug.a_deftype.A
 at clojure.core$_cache_protocol_fn.invokeStatic (core_deftype.clj:583)
    clojure.core$_cache_protocol_fn.invoke (core_deftype.clj:575)
    lein_test_reload_bug.b_protocol$eval473$fn__474$G__464__479.invoke (b_protocol.clj:5)
    lein_test_reload_bug.core_test$fn__502.invokeStatic (core_test.clj:34)
    lein_test_reload_bug.core_test/fn (core_test.clj:28)
    clojure.test$test_var$fn__9737.invoke (test.clj:717)
    clojure.test$test_var.invokeStatic (test.clj:717)
    clojure.test$test_var.invoke (test.clj:708)
    clojure.test$test_vars$fn__9763$fn__9768.invoke (test.clj:735)
    clojure.test$default_fixture.invokeStatic (test.clj:687)
    clojure.test$default_fixture.invoke (test.clj:683)
    clojure.test$test_vars$fn__9763.invoke (test.clj:735)
    clojure.test$default_fixture.invokeStatic (test.clj:687)
    clojure.test$default_fixture.invoke (test.clj:683)
    clojure.test$test_vars.invokeStatic (test.clj:731)
    clojure.test$test_all_vars.invokeStatic (test.clj:737)
    clojure.test$test_ns.invokeStatic (test.clj:758)
    clojure.test$test_ns.invoke (test.clj:743)
    user$eval224$fn__287.invoke (form-init5634774907020355671.clj:1)
    clojure.lang.AFn.applyToHelper (AFn.java:156)
    clojure.lang.AFn.applyTo (AFn.java:144)
    clojure.core$apply.invokeStatic (core.clj:667)
    clojure.core$apply.invoke (core.clj:660)
    leiningen.core.injected$compose_hooks$fn__154.doInvoke (form-init5634774907020355671.clj:1)
    clojure.lang.RestFn.applyTo (RestFn.java:137)
    clojure.core$apply.invokeStatic (core.clj:665)
    clojure.core$apply.invoke (core.clj:660)
    leiningen.core.injected$run_hooks.invokeStatic (form-init5634774907020355671.clj:1)
    leiningen.core.injected$run_hooks.invoke (form-init5634774907020355671.clj:1)
    leiningen.core.injected$prepare_for_hooks$fn__159$fn__160.doInvoke (form-init5634774907020355671.clj:1)
    clojure.lang.RestFn.applyTo (RestFn.java:137)
    clojure.lang.AFunction$1.doInvoke (AFunction.java:31)
    clojure.lang.RestFn.invoke (RestFn.java:408)
    clojure.core$map$fn__5866.invoke (core.clj:2755)
    clojure.lang.LazySeq.sval (LazySeq.java:42)
    clojure.lang.LazySeq.seq (LazySeq.java:51)
    clojure.lang.Cons.next (Cons.java:39)
    clojure.lang.RT.boundedLength (RT.java:1792)
    clojure.lang.RestFn.applyTo (RestFn.java:130)
    clojure.core$apply.invokeStatic (core.clj:667)
    clojure.test$run_tests.invokeStatic (test.clj:768)
    clojure.test$run_tests.doInvoke (test.clj:768)
    clojure.lang.RestFn.applyTo (RestFn.java:137)
    clojure.core$apply.invokeStatic (core.clj:665)
    clojure.core$apply.invoke (core.clj:660)
    user$eval224$fn__299$fn__332.invoke (form-init5634774907020355671.clj:1)
    user$eval224$fn__299$fn__300.invoke (form-init5634774907020355671.clj:1)
    user$eval224$fn__299.invoke (form-init5634774907020355671.clj:1)
    user$eval224.invokeStatic (form-init5634774907020355671.clj:1)
    user$eval224.invoke (form-init5634774907020355671.clj:1)
    clojure.lang.Compiler.eval (Compiler.java:7177)
    clojure.lang.Compiler.eval (Compiler.java:7167)
    clojure.lang.Compiler.load (Compiler.java:7636)
    clojure.lang.Compiler.loadFile (Compiler.java:7574)
    clojure.main$load_script.invokeStatic (main.clj:475)
    clojure.main$init_opt.invokeStatic (main.clj:477)
    clojure.main$init_opt.invoke (main.clj:477)
    clojure.main$initialize.invokeStatic (main.clj:508)
    clojure.main$null_opt.invokeStatic (main.clj:542)
    clojure.main$null_opt.invoke (main.clj:539)
    clojure.main$main.invokeStatic (main.clj:664)
    clojure.main$main.doInvoke (main.clj:616)
    clojure.lang.RestFn.applyTo (RestFn.java:137)
    clojure.lang.Var.applyTo (Var.java:705)
    clojure.main.main (main.java:40)

Ran 1 tests containing 1 assertions.
0 failures, 1 errors.
Tests failed.
```

## License

Copyright © 2020 Ambrose Bonnaire-Sergeant

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
