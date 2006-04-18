package ch.mimo.eclipse.plugin.logfiletools.viewer;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import ch.mimo.eclipse.plugin.logfiletools.LogFileViewPlugin;
import ch.mimo.eclipse.plugin.logfiletools.Logger;
import ch.mimo.eclipse.plugin.logfiletools.viewer.rule.TokenData;

/*
 * Copyright (c) 2006 by Michael Mimo Moratti
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

public class DamageRepairer implements IPresentationDamager, IPresentationRepairer {
    
    // Attribute --------------------------------------------------------------------
    
    private Logger logger;
    private ITokenScanner scanner;
    private TextAttribute defaultTextAttribute;
    private IDocument document;
    private List coloredRanges;

    // Constructor ------------------------------------------------------------------
    
    public DamageRepairer(ITokenScanner scanner) {
        logger = LogFileViewPlugin.getDefault().getLogger();
        Assert.isNotNull(scanner);
        this.scanner = scanner;
        coloredRanges = new Vector();
        defaultTextAttribute = new TextAttribute(null);
    }
    
    // Public -----------------------------------------------------------------------
    
    public void setDocument(IDocument document) {
        this.document = document;
    }

    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
        if (!documentPartitioningChanged) {
            try {

                IRegion info= document.getLineInformationOfOffset(event.getOffset());
                int start= Math.max(partition.getOffset(), info.getOffset());

                int end= event.getOffset() + (event.getText() == null ? event.getLength() : event.getText().length());

                if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
                    // optimize the case of the same line
                    end= info.getOffset() + info.getLength();
                } else
                    end= endOfLineOf(end);

                end= Math.min(partition.getOffset() + partition.getLength(), end);
                return new Region(start, end - start);

            } catch (BadLocationException x) {
                logger.logInfo("unable to find location in document to repair a given region",x); //$NON-NLS-1$
            }
        }
        return partition;
    }

    public void createPresentation(TextPresentation presentation, ITypedRegion region) {
        int lastStart= region.getOffset();
        int length= 0;
        boolean firstToken= true;
        IToken lastToken= Token.UNDEFINED;
        int lastPriority = 0;
        TextAttribute lastAttribute= getTokenTextAttribute(lastToken);

        scanner.setRange(document,lastStart,region.getLength());

        while (true) {
            IToken token= scanner.nextToken();
            if (token.isEOF()) {
                break;
            }

            TextAttribute attribute= getTokenTextAttribute(token);
            int priority = getTokenPriority(token);
            if (lastAttribute != null && lastAttribute.equals(attribute)) {
                length += scanner.getTokenLength();
                firstToken = false;
            } else {
                if (!firstToken) {
                    addRange(presentation,lastStart,length,lastAttribute,true,lastPriority);
                }
                firstToken= false;
                lastToken= token;
                lastAttribute= attribute;
                lastStart= scanner.getTokenOffset();
                length= scanner.getTokenLength();
                lastPriority = priority;
            }
        }
        addRange(presentation,lastStart,length,lastAttribute,true,lastPriority);
    }
    
    // Private ----------------------------------------------------------------------
    
    /**
     * Returns the end offset of the line that contains the specified offset or
     * if the offset is inside a line delimiter, the end offset of the next line.
     *
     * @param offset the offset whose line end offset must be computed
     * @return the line end offset for the given offset
     * @exception BadLocationException if offset is invalid in the current document
     */
    private int endOfLineOf(int offset) throws BadLocationException {

        IRegion info= document.getLineInformationOfOffset(offset);
        if (offset <= info.getOffset() + info.getLength())
            return info.getOffset() + info.getLength();

        int line= document.getLineOfOffset(offset);
        try {
            info= document.getLineInformation(line + 1);
            return info.getOffset() + info.getLength();
        } catch (BadLocationException x) {
            return document.getLength();
        }
    }
    
    /**
     * Returns a text attribute encoded in the given token. If the token's
     * data is not <code>null</code> and a text attribute it is assumed that
     * it is the encoded text attribute. It returns the default text attribute
     * if there is no encoded text attribute found.
     *
     * @param token the token whose text attribute is to be determined
     * @return the token's text attribute
     */
    private TextAttribute getTokenTextAttribute(IToken token) {
        Object data= token.getData();
        if(data instanceof TokenData) {
            return ((TokenData)data).getTextAttribute();
        }
        return defaultTextAttribute;
    }
    
    private int getTokenPriority(IToken token) {
        Object data= token.getData();
        if(data instanceof TokenData) {
            return ((TokenData)data).getPriority();
        }
        return 0;
    }
    
    /**
     * Adds style information to the given text presentation.
     *
     * @param presentation the text presentation to be extended
     * @param offset the offset of the range to be styled
     * @param length the length of the range to be styled
     * @param attr the attribute describing the style of the range to be styled
     * @param wholeLine the boolean switch to declare that the whole line should be colored
     */
    private void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr, boolean wholeLine, int priority) {
        if (attr != null) {
            int style= attr.getStyle();
            int fontStyle= style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
            if(wholeLine) {
                try {
                    int line = document.getLineOfOffset(offset);
                    int start = document.getLineOffset(line);
                    length = document.getLineLength(line);
                    offset = start;
                } catch (BadLocationException e) {
                }
            }
            Range range = new Range(priority,offset,length);
            if(isCurrentRangePriorityHigherThanAllreadyExistingRange(presentation,range)) {
                coloredRanges.add(range);
                StyleRange styleRange = new StyleRange(offset,length,attr.getForeground(),attr.getBackground(),fontStyle);
                styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
                styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
                presentation.addStyleRange(styleRange);
            }
        }
    }
    
    /**
     * this method evaluates all already applied ranges against the new range that is to be 
     * applied. If the priority of the to be applied range is lower than an allready existing
     * range than the return value will be false.
     * 
     * @param presentation
     * @param range
     * @return boolean true if the to be check range has a hihger priority than an existing
     * range.
     */
    private boolean isCurrentRangePriorityHigherThanAllreadyExistingRange(TextPresentation presentation, Range range) {
        if(presentation.getDenumerableRanges() == 0) {
            coloredRanges.clear();
            return true;
        }
        Iterator rangeIterator = coloredRanges.iterator();
        while(rangeIterator.hasNext()) {
            Range storedRange = (Range)rangeIterator.next();
            if(range.equals(storedRange)) {
            	return range.isHigherPriorityThan(storedRange);
            }
        }
        return true;
    }
    
    // Inner Class ------------------------------------------------------------------
    
    private class Range {
        
        // Attribute --------------------------------------------------
        
        private int priority;
        private int offset;
        private int length;
        
        // Constructor ------------------------------------------------
        
        public Range(int priority, int offset, int length) {
            this.priority = priority;
            this.offset = offset;
            this.length = length;
        }
        
        // Public -----------------------------------------------------
        
        /**
         * @return Returns the length.
         */
        public int getLength() {
            return length;
        }

        /**
         * @return Returns the offset.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * @return Returns the priority.
         */
        public int getPriority() {
            return priority;
        }
        
        /**
         * this method does not consider the priorty!
         */
        public boolean equals(Object object) {
            if(!(object instanceof Range)) {
                return false;
            }
            Range range = (Range)object;
            if(range.getOffset() == this.getOffset() & range.getLength() == this.getLength()) {
                return true;
            }
            return false;
        }
        
        /**
         * higher priority means: 0 wins over 1
         */
        public boolean isHigherPriorityThan(Range range) {
            return this.getPriority() < range.getPriority();
        }
     }
}
