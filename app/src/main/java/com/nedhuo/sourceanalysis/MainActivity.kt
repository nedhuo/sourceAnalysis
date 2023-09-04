package com.nedhuo.sourceanalysis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageView
import com.bumptech.glide4110.Glide

/**
 *
 * 1. Fragment 创建时提交事物 commit()commitAllowingStateLoss()不会立即执行 内部利用的是Handler的消息机制
 * handler.obtainMessage(...).sendToTarget()会获取Message去sendMessage  可以立即执行 ？
 *
 * 2. Hanler的生命周期监控Fragment保证唯一性 是通过一个集合
 *     首先尝试从集合中获取  然后创建空白Fragment  再将FragmentManager 与 Fragment 组成key value放入map
 *     fragment 提交事物添加Fragment（通过Handler 可能不会立即执行）调用handler.obtainMessage(...).sendToTarget()
 * 3.
 *      @VisibleForTesting
 *   final Map<FragmentManager, SupportRequestManagerFragment> pendingSupportRequestManagerFragments =
 *       new HashMap<>();
 *
 *   @NonNull
 *   private SupportRequestManagerFragment getSupportRequestManagerFragment(
 *       @NonNull final FragmentManager fm, @Nullable Fragment parentHint, boolean isParentVisible) {
 *
 *       //1. 通过当前页面的FragmentManager通过Tag获取Fragment 如果之前创建过，Fragment就不为空
 *     SupportRequestManagerFragment current =
 *         (SupportRequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
 *     if (current == null) {
 *
 *     //2. pendingSupportRequestManagerFragments是一个Map集合 用来保证Fragment不会重复创建 先从缓存Map中
 *     //取Fragment
 *       current = pendingSupportRequestManagerFragments.get(fm);
 *
 *       if (current == null) {
 *       //3. 创建Fragment
 *         current = new SupportRequestManagerFragment();
 *         current.setParentFragmentHint(parentHint);
 *         if (isParentVisible) {
 *           current.getGlideLifecycle().onStart();
 *         }
 *
 *         //4.
 *         // 第一步：这个地方将创建的Fragment保存进缓存Map
 *         // 第二步：然后在当前页面添加空白Fragment（commitAllowingStateLoss是一个异步提交操作，原理是通过Handler进行提交）
 *         // 第三步：然后提交之后在发送一个Handler事件移除缓存Map中保存的 Fragment
 *         // 这三部的原理是防止在异步提交Fragment的时候 下个调用进来，getTag为空 重新创建Fragment 再次提交，添加
 *         //多个Fragment的情况，当Fragment成功添加后 getTag就不会为空 下面逻辑就不会调用
 *         pendingSupportRequestManagerFragments.put(fm, current);
 *         fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
 *         handler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget();
 *       }
 *     }
 *     return current;
 *   }
 *   这部分代码是Glide获取空白Fragment的代码
 *
 *
 *   3.1 只有在主线程调用的Glide.with()才会在当前页面添加空白Fragment进行生命周期监听
 */
class MainActivity private constructor(): AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //RequestManager        //BaseRequestOptions
        Glide.with(this).load("").into(ImageView(this))

        //Glide创建资自身对象，获取RequestManagerRetriever 通过RequestManagerRetriever获取RequestManager

        //创建了一个RequestBuilder  并返回BaseRequestOptions对象（RequestBuilder继承BaseRequestOptions） 又强转为RequestBuilder

        //DrawableImageViewTarget  将Request对象放在了ViewTarget中    请求部分 请求构建以及请求完成回调值给Target

//        supportFragmentManager.beginTransaction().add(Fragment(),FRAGMENT_TAG).commitAllowingStateLoss()
//
//        Handler().obtainMessage(ID_REMOVE_FRAGMENT_MANAGER, supportFragmentManager).sendToTarget()
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }


    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }

}