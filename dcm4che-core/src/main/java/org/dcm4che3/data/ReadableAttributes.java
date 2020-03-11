

package org.dcm4che3.data;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.dcm4che3.data.Attributes.Visitor;
import org.dcm4che3.data.IOD.DataElement;
import org.dcm4che3.io.DicomOutputStream;

/**
 * This interface can be used as read-only view of {@link Attributes}.
 * <p>
 * The idea is to allow multi-threaded applications safe access to read the
 * dataset, without allowing modifications.
 * 
 * <p>
 * Note that this is not completely safe:
 * <ul>
 * <li>the underlying Attributes object can still be modified, if a reference to
 * it is kept</li>
 * <li>using a cast to Attributes, the read-only behavior can be circumvented.
 * Users should not do this.</li>
 * <li>some methods (e.g. {@link #getBytes} and {@link #getValue}) return
 * modifiable internals of the Attributes. The user is responsible to not modify
 * those.</li>
 * </ul>
 * <p>
 * As safety is not guaranteed, this should rather be seen as a marker
 * interface, to document to users of an API that a returned object should only
 * be read and never modified.
 * 
 * <p>
 * To make a (safe) modifiable copy of an {@link ReadableAttributes} a copy
 * constructor can be used ({@link Attributes#Attributes(ReadableAttributes)}).
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public interface ReadableAttributes {

    Object getProperty(String key, Object defVal);

    int diff(ReadableAttributes other, int[] selection, Attributes diff, boolean onlyModified);

    int diff(ReadableAttributes other, int[] selection, Attributes diff);

    Attributes getRemovedOrModified(ReadableAttributes other);

    Attributes getModified(ReadableAttributes other, Attributes result);

    void validate(DataElement el, ValidationResult result);

    ValidationResult validate(IOD iod);

    boolean matches(ReadableAttributes keys, boolean ignorePNCase, boolean matchNoValue);

    Attributes createFileMetaInformation(String tsuid);

    void writeGroupTo(DicomOutputStream out, int groupLengthTag) throws IOException;

    boolean accept(Visitor visitor, boolean visitNestedDatasets) throws Exception;

    void writeItemTo(DicomOutputStream out) throws IOException;

    void writeTo(DicomOutputStream out) throws IOException;

    StringBuilder toStringBuilder(int limit, int maxWidth, StringBuilder sb);

    StringBuilder toStringBuilder(StringBuilder sb);

    String toString(int limit, int maxWidth);

    boolean equalValues(ReadableAttributes other, String privateCreator, int tag);

    boolean equalValues(ReadableAttributes other, int tag);

    String getPrivateCreator(int tag);

    TimeZone getTimeZone();

    TimeZone getDefaultTimeZone();

    SpecificCharacterSet getSpecificCharacterSet();

    DateRange getDateRange(String privateCreator, long tag, DateRange defVal);

    DateRange getDateRange(String privateCreator, long tag);

    DateRange getDateRange(long tag, DateRange defVal);

    DateRange getDateRange(long tag);

    DateRange getDateRange(String privateCreator, int tag, VR vr, DateRange defVal);

    DateRange getDateRange(String privateCreator, int tag, VR vr);

    DateRange getDateRange(String privateCreator, int tag, DateRange defVal);

    DateRange getDateRange(String privateCreator, int tag);

    DateRange getDateRange(int tag, DateRange defVal);

    DateRange getDateRange(int tag);

    Date[] getDates(String privateCreator, long tag, DatePrecisions precisions);

    Date[] getDates(String privateCreator, long tag);

    Date[] getDates(long tag, DatePrecisions precisions);

    Date[] getDates(long tag);

    Date[] getDates(String privateCreator, int tag, VR vr, DatePrecisions precisions);

    Date[] getDates(String privateCreator, int tag, VR vr);

    Date[] getDates(String privateCreator, int tag, DatePrecisions precisions);

    Date[] getDates(String privateCreator, int tag);

    Date[] getDates(int tag, DatePrecisions precisions);

    Date[] getDates(int tag);

    Date getDate(String privateCreator, long tag, Date defVal, DatePrecision precision);

    Date getDate(String privateCreator, long tag, Date defVal);

    Date getDate(String privateCreator, long tag, DatePrecision precision);

    Date getDate(String privateCreator, long tag);

    Date getDate(long tag, Date defVal, DatePrecision precision);

    Date getDate(long tag, Date defVal);

    Date getDate(long tag, DatePrecision precision);

    Date getDate(long tag);

    Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal, DatePrecision precision);

    Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal);

    Date getDate(String privateCreator, int tag, VR vr, int valueIndex, DatePrecision precision);

    Date getDate(String privateCreator, int tag, VR vr, int valueIndex);

    Date getDate(String privateCreator, int tag, int valueIndex, Date defVal, DatePrecision precision);

    Date getDate(String privateCreator, int tag, int valueIndex, Date defVal);

    Date getDate(String privateCreator, int tag, int valueIndex, DatePrecision precision);

    Date getDate(String privateCreator, int tag, int valueIndex);

    Date getDate(String privateCreator, int tag, VR vr, Date defVal, DatePrecision precision);

    Date getDate(String privateCreator, int tag, VR vr, Date defVal);

    Date getDate(String privateCreator, int tag, VR vr, DatePrecision precision);

    Date getDate(String privateCreator, int tag, VR vr);

    Date getDate(String privateCreator, int tag, Date defVal, DatePrecision precision);

    Date getDate(String privateCreator, int tag, DatePrecision precision);

    Date getDate(String privateCreator, int tag);

    Date getDate(int tag, int valueIndex, Date defVal, DatePrecision precision);

    Date getDate(int tag, int valueIndex, Date defVal);

    Date getDate(int tag, int valueIndex, DatePrecision precision);

    Date getDate(int tag, int valueIndex);

    Date getDate(int tag, Date defVal, DatePrecision precision);

    Date getDate(int tag, Date defVal);

    Date getDate(int tag, DatePrecision precision);

    Date getDate(int tag);

    double[] getDoubles(String privateCreator, int tag, VR vr);

    double[] getDoubles(String privateCreator, int tag);

    double[] getDoubles(int tag);

    double getDouble(String privateCreator, int tag, VR vr, int valueIndex, double defVal);

    double getDouble(String privateCreator, int tag, int valueIndex, double defVal);

    double getDouble(String privateCreator, int tag, VR vr, double defVal);

    double getDouble(String privateCreator, int tag, double defVal);

    double getDouble(int tag, int valueIndex, double defVal);

    double getDouble(int tag, double defVal);

    float[] getFloats(String privateCreator, int tag, VR vr);

    float[] getFloats(String privateCreator, int tag);

    float[] getFloats(int tag);

    float getFloat(String privateCreator, int tag, VR vr, int valueIndex, float defVal);

    float getFloat(String privateCreator, int tag, int valueIndex, float defVal);

    float getFloat(String privateCreator, int tag, VR vr, float defVal);

    float getFloat(String privateCreator, int tag, float defVal);

    float getFloat(int tag, int valueIndex, float defVal);

    float getFloat(int tag, float defVal);

    int[] getInts(String privateCreator, int tag, VR vr);

    int[] getInts(String privateCreator, int tag);

    int[] getInts(int tag);

    int getInt(String privateCreator, int tag, VR vr, int valueIndex, int defVal);

    int getInt(String privateCreator, int tag, int valueIndex, int defVal);

    int getInt(String privateCreator, int tag, VR vr, int defVal);

    int getInt(String privateCreator, int tag, int defVal);

    int getInt(int tag, int valueIndex, int defVal);

    int getInt(int tag, int defVal);

    String[] getStrings(String privateCreator, int tag, VR vr);

    String[] getStrings(String privateCreator, int tag);

    String[] getStrings(int tag);

    String getString(String privateCreator, int tag, VR vr, int valueIndex, String defVal);

    String getString(String privateCreator, int tag, VR vr, int valueIndex);

    String getString(String privateCreator, int tag, int valueIndex, String defVal);

    String getString(String privateCreator, int tag, int valueIndex);

    String getString(String privateCreator, int tag, VR vr, String defVal);

    String getString(String privateCreator, int tag, VR vr);

    String getString(String privateCreator, int tag, String defVal);

    String getString(String privateCreator, int tag);

    String getString(int tag, int valueIndex, String defVal);

    String getString(int tag, int valueIndex);

    String getString(int tag, String defVal);

    String getString(int tag);

    byte[] getSafeBytes(String privateCreator, int tag);

    byte[] getSafeBytes(int tag);

    byte[] getBytes(String privateCreator, int tag) throws IOException;

    byte[] getBytes(int tag) throws IOException;

    // TODO here I want to return a ReadableSequence, which does not allow modifications
    Sequence getSequence(String privateCreator, int tag);

    Sequence getSequence(int tag);

    VR getVR(String privateCreator, int tag);

    VR getVR(int tag);

    Object getValue(String privateCreator, int tag, VR.Holder vr);

    Object getValue(String privateCreator, int tag);

    Object getValue(int tag, VR.Holder vr);

    Object getValue(int tag);

    String privateCreatorOf(int tag);

    /**
     * Test whether at least one tag within the given range is contained.
     * 
     * @param firstTag
     *            first tag (inclusive)
     * @param lastTag
     *            last tag (inclusive)
     * @return whether at least one tag within the given range is contained
     */
    boolean containsTagInRange(int firstTag, int lastTag);

    boolean containsValue(String privateCreator, int tag);

    boolean containsValue(int tag);

    boolean contains(String privateCreator, int tag);

    boolean contains(int tag);

    SpecificCharacterSet getSpecificCharacterSet(VR vr);

    int tagOf(String privateCreator, int tag);

    ReadableAttributes getNestedDataset(List<ItemPointer> itemPointers);

    ReadableAttributes getNestedDataset(ItemPointer... itemPointers);

    ReadableAttributes getNestedDataset(String privateCreator, int sequenceTag, int itemIndex);

    ReadableAttributes getNestedDataset(String privateCreator, int sequenceTag);

    ReadableAttributes getNestedDataset(int sequenceTag, int itemIndex);

    ReadableAttributes getNestedDataset(int sequenceTag);

    int size();

    boolean isEmpty();

    long getItemPosition();

    int getLength();

    ReadableAttributes getRoot();

    ReadableAttributes getParent();

    boolean bigEndian();

    int getLevel();

    boolean isRoot();

    int indexOf(int tag);

    public int indexOf(String privateCreator, int tag);

    public int creatorTagOf(String privateCreator, int tag);

    public int[] tags();
}
