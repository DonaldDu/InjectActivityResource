# InjectActivityResource

研究Qigsaw独立打包，开始发现可以在ActivityLifecycleCallbacks.onActivityPreCreated中注入Resources，后来发现只有Android10+才行，以前的没有，IDE没提示所以一直没发现。

不想在编译期间织入代码，想要做到独立打包，所以想找个兼容所有版本的方法。
如果定义一个基类并在其中重写 getResources 方法是最简单且最高效的办法。但是引用三方库时，没法解决了。
后来想到一个办法：绝大部分的三方库的基类都是AppCompatActivity，如果在公认的基类中增加代码来实现资源注入，那就太方便了。

研究发现这个办法是确实可行的，于是就写代码了。
最后原理是这样的：

绝大部分的项目都引用了appcompat，现在把appcompat库中的AppCompatActivity类通过ASM修改增加一个注入资源的方法。然后把修改后的库打包为 **appcompat.qb** ，其它保持不变。再把库发布到本地或公司仓库。最后在项目中替换为新的依赖。

```
    implementation 'androidx.appcompat:appcompat:1.2.0'//大部分的项目用这个
    implementation 'androidx.appcompat:appcompat.qb:1.2.0'//替换为这个
```
