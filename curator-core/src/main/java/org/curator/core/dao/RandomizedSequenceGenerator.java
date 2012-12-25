package org.curator.core.dao;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;

public class RandomizedSequenceGenerator extends SequenceStyleGenerator {

    private int suffixLength = 4;

    public int getSuffixLength() {
        return suffixLength;
    }

    public void setSuffixLength(int suffixLength) {
        this.suffixLength = suffixLength;
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        String ts = String.valueOf(System.currentTimeMillis());
        String randomSuffix = ts.substring(ts.length() - getSuffixLength(), ts.length());
        return Long.parseLong(super.generate(session, object) + randomSuffix);
    }

}

