package cn.nihility.serialize;

import cn.nihility.exception.IllegalParseException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements Serializer {

    /*
     * 将 kryo 对象存储在 ThreadLocal，保证 kryo 的线程安全性，ThreadLocal (线程内部存储类)
     * 通过 get()&set() 方法读取线程内的数据
     * */
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        /*
         * 引用，对 A 对象序列化时，默认情况下 kryo 会在每个成员对象第一次序列化时写入一个数字
         * 该数字逻辑上就代表了对该成员对象的引用，如果后续有引用指向该成员对象
         * 则直接序列化之前存入的数字即可，而不需要再次序列化对象本身。
         * 这种默认策略对于成员存在互相引用的情况较有利，否则就会造成空间浪费
         * （因为每序列化一个成员对象，都多序列化一个数字）
         * 通常情况下可以将该策略关闭，kryo.setReferences(false);
         * */
        kryo.setReferences(false);
        // 设置是否注册全限定名
        kryo.setRegistrationRequired(false);
        // 设置初始化策略，如果没有默认无参构造器，那么就需要设置此项,使用此策略构造一个无参构造器
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    public static Kryo borrow() {
        return kryoThreadLocal.get();
    }

    public static void release() {
        kryoThreadLocal.remove();
    }

    @Override
    public byte[] serialize(Object obj) throws IllegalParseException {
        Kryo kryo = borrow();
        kryo.register(obj.getClass());
        try (final ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
             final Output out = new Output(bao)) {
            kryo.writeObject(out, obj);
            out.flush();
            return bao.toByteArray();
        } catch (IOException e) {
            throw new IllegalParseException(e.getMessage(), e);
        } finally {
            release();
        }
    }

    @Override
    public <T> T deserialize(byte[] src, Class<T> type) throws IllegalParseException {
        final Kryo kryo = borrow();
        kryo.register(type);
        try (final ByteArrayInputStream bai = new ByteArrayInputStream(src);
             final Input in = new Input(bai)) {
            return kryo.readObject(in, type);
        } catch (IOException e) {
            throw new IllegalParseException(e.getMessage(), e);
        } finally {
            release();
        }
    }

}
