package it.unibz.inf.pm.ocel.id;

import java.io.*;
import java.util.UUID;


public class OcelID implements Cloneable, Comparable<OcelID> {
    private final UUID uuid;

    public OcelID() {
        this.uuid = UUID.randomUUID();
    }

    public OcelID(long msb, long lsb) {
        this.uuid = new UUID(msb, lsb);
    }

    public OcelID(UUID uuid) {
        this.uuid = uuid;
    }

    public static OcelID parse(String idString) {
        UUID uuid = UUID.fromString(idString);
        return new OcelID(uuid);
    }

    public static OcelID read(DataInputStream dis) throws IOException {
        long msb = dis.readLong();
        long lsb = dis.readLong();
        return new OcelID(msb, lsb);
    }

    public static OcelID read(DataInput in) throws IOException {
        long msb = in.readLong();
        long lsb = in.readLong();
        return new OcelID(msb, lsb);
    }

    public static void write(OcelID id, DataOutputStream dos) throws IOException {
        dos.writeLong(id.uuid.getMostSignificantBits());
        dos.writeLong(id.uuid.getLeastSignificantBits());
    }

    public static void write(OcelID id, DataOutput out) throws IOException {
        out.writeLong(id.uuid.getMostSignificantBits());
        out.writeLong(id.uuid.getLeastSignificantBits());
    }

    public boolean equals(Object obj) {
        if (obj instanceof OcelID) {
            OcelID other = (OcelID)obj;
            return this.uuid.equals(other.uuid);
        } else {
            return false;
        }
    }

    public String toString() {
        return this.uuid.toString().toUpperCase();
    }

    public Object clone() {
        OcelID clone;
        try {
            clone = (OcelID)super.clone();
        } catch (CloneNotSupportedException var3) {
            var3.printStackTrace();
            clone = null;
        }

        return clone;
    }

    public int hashCode() {
        return this.uuid.hashCode();
    }

    public int compareTo(OcelID o) {
        return this.uuid.compareTo(o.uuid);
    }
}

