# lein-test-reload-bug

Reproduces `lein test` reloading namespaces too often.

This bug stems from the fact that
```clojure
  (require :reload 'A 'B)
```
will load B twice if A depends on B.

This repository attempts to reproduce this reliably to demonstrate
that `lein test`'s use of require :reload [here](https://github.com/technomancy/leiningen/blob/df7122f6aa5599f515b66522ade87c92777b25f2/src/leiningen/test.clj#L86) is problematic.

## Bug report

**Initial debugging steps**
Before creating a report, _especially_ around exceptions being thrown when running Leiningen, please check if the error still occurs after:

- [x] Updating to using the latest released version of Leiningen (`lein upgrade`).
- [x] Moving your `~/.lein/profiles.clj` (if present) out of the way. This contains third-party dependencies and plugins that can cause problems inside Leiningen.
- [x] Updating any old versions of plugins in your `project.clj`, especially if the problem is with a plugin not working. Old versions of plugins like nREPL and CIDER (as well as others) can cause problems with newer versions of Leiningen.
- [x] (If you are using Java 9 or newer), updating your dependencies to their most recent versions. Recent JDK's have introduced changes which can break some Clojure libraries.

**Describe the bug**
`lein test` can reload namespaces out of order. This can leave deftypes implementing expired protocol interfaces, and thus an exception will be thrown when attempting to invoke a protocol method.

This is also a potential performance problem, since a namespace maybe reloaded twice.

**To Reproduce**
A possible cause of this bug is that `lein test` uses clojure.core/require's :reload flag.

Working backwards from there, a simple way to make `require :reload` reload things out of order is to call

```clojure
(require :reload 'A 'B)
```

where A depends on B. B will be loaded twice, first as a dependency of A, then via the `:reload` logic.

If B contains a protocol and A contains a deftype, then the deftype will implement a now-stale interface.

This scenario is demonstrated [here](https://github.com/frenchy64/lein-test-reload-bug), and can be triggered with `lein test`.

The concrete scenario involves 2 namespaces:

```clojure
(ns lein-test-reload-bug.a-deftype
  (:require [lein-test-reload-bug.b-protocol
             :refer [B]]))

(deftype A []
  B
  (b [this]))
```

```clojure
(ns lein-test-reload-bug.b-protocol)

(defprotocol B
  (b [this]))
```

Notice how this matches the hypothetical scenario, except
- A = lein-test-reload-bug.a-deftype
- B = lein-test-reload-bug.b-protocol

The following require is called by `lein test`:

```clojure
(require :reload 'lein-test-reload-bug.a-deftype
                 'lein-test-reload-bug.b-protocol)
```

**Actual behavior**
`lein-test-reload-bug.b-protocol` is loaded twice, the second time is _after_ `lein-test-reload-bug.a-deftype`, thus leaving it in a bad state.

It is now impossible to make instances of A that can be called via the protocol, because it implements an old protocol.

Relevant output from sample project: 

```clojure
lein test lein-test-reload-bug.core-test
"The current hash of interface lein_test_reload_bug.b_protocol.B is" 1214133948
"The current instance of A implements lein_test_reload_bug.b_protocol.B with hash" -1634164376

ERROR in (a-test) (core_deftype.clj:583)
Uncaught exception, not in assertion.
expected: nil
  actual: java.lang.IllegalArgumentException: No implementation of method: :b of protocol: #'lein-test-reload-bug.b-protocol/B found for class: lein_test_reload
_bug.a_deftype.A
 at clojure.core$_cache_protocol_fn.invokeStatic (core_deftype.clj:583)
    clojure.core$_cache_protocol_fn.invoke (core_deftype.clj:575)
    lein_test_reload_bug.b_protocol$eval418$fn__419$G__409__424.invoke (b_protocol.clj:5)
    lein_test_reload_bug.core_test$fn__448.invokeStatic (core_test.clj:39)
<SNIP>
```

See `lein-test-reload-bug.core-test/a-test` for exact test setup, but it boils down to the familiar "Foo is not Foo" problem.

**Expected behavior**
`lein-test-reload-bug.b-protocol` is loaded exactly once.

**Link to sample project**
https://github.com/frenchy64/lein-test-reload-bug

**Logs**
- [Full reproduction log](https://github.com/frenchy64/lein-test-reload-bug#reproduction)

**Environment**
- Leiningen Version: `Leiningen 2.9.5 on Java 1.8.0_275 OpenJDK 64-Bit Server VM`
- Leiningen installation method: manual
- JDK Version:
```
openjdk version "1.8.0_275"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_275-b01)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.275-b01, mixed mode)
```
- OS: macOS 10.15.7

**Additional context**

## Reproduction

See `lein-test-reload-bug.core-test` for explanation.

```
bash-5.0$ lein version
Leiningen 2.9.5 on Java 1.8.0_275 OpenJDK 64-Bit Server VM
bash-5.0$ java -version
openjdk version "1.8.0_275"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_275-b01)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.275-b01, mixed mode)
bash-5.0$ lein test
"loading" lein-test-reload-bug.a-deftype
"loading" lein-test-reload-bug.b-protocol
"loading" lein-test-reload-bug.b-protocol
"loading" lein-test-reload-bug.core-test

lein test lein-test-reload-bug.core-test
"The current hash of interface lein_test_reload_bug.b_protocol.B is" 1214133948
"The current instance of A implements lein_test_reload_bug.b_protocol.B with hash" -1634164376

lein test :only lein-test-reload-bug.core-test/a-test

ERROR in (a-test) (core_deftype.clj:583)
Uncaught exception, not in assertion.
expected: nil
  actual: java.lang.IllegalArgumentException: No implementation of method: :b of protocol: #'lein-test-reload-bug.b-protocol/B found for class: lein_test_reload
_bug.a_deftype.A
 at clojure.core$_cache_protocol_fn.invokeStatic (core_deftype.clj:583)
    clojure.core$_cache_protocol_fn.invoke (core_deftype.clj:575)
    lein_test_reload_bug.b_protocol$eval418$fn__419$G__409__424.invoke (b_protocol.clj:5)
    lein_test_reload_bug.core_test$fn__448.invokeStatic (core_test.clj:39)
    lein_test_reload_bug.core_test/fn (core_test.clj:29)
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
    user$eval224$fn__287.invoke (form-init5338889162230199148.clj:1)
    clojure.lang.AFn.applyToHelper (AFn.java:156)
    clojure.lang.AFn.applyTo (AFn.java:144)
    clojure.core$apply.invokeStatic (core.clj:667)
    clojure.core$apply.invoke (core.clj:660)
    leiningen.core.injected$compose_hooks$fn__154.doInvoke (form-init5338889162230199148.clj:1)
    clojure.lang.RestFn.applyTo (RestFn.java:137)
    clojure.core$apply.invokeStatic (core.clj:665)
    clojure.core$apply.invoke (core.clj:660)
    leiningen.core.injected$run_hooks.invokeStatic (form-init5338889162230199148.clj:1)
    leiningen.core.injected$run_hooks.invoke (form-init5338889162230199148.clj:1)
    leiningen.core.injected$prepare_for_hooks$fn__159$fn__160.doInvoke (form-init5338889162230199148.clj:1)
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
    user$eval224$fn__299$fn__332.invoke (form-init5338889162230199148.clj:1)
    user$eval224$fn__299$fn__300.invoke (form-init5338889162230199148.clj:1)
    user$eval224$fn__299.invoke (form-init5338889162230199148.clj:1)
    user$eval224.invokeStatic (form-init5338889162230199148.clj:1)
    user$eval224.invoke (form-init5338889162230199148.clj:1)
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
bash-5.0$
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
