package qrcode;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class MatrixConstruction {

	/*
	 * Constants defining the color in ARGB format
	 * 
	 * W = White integer for ARGB
	 * 
	 * B = Black integer for ARGB
	 * 
	 * both needs to have their alpha component to 255
	 */
	private final static int WHITE_COLOR = 0xFF_FF_FF_FF;
	private final static int BLACK_COLOR = 0xFF_00_00_00;
	private final static int RED_COLOR   = 0xAA_AA_22_55;
	private final static int GREEN_COLOR = 0xAA_AA_CC_AA;

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @param mask
	 *            The mask used on the data. If not valid (e.g: -1), then no mask is
	 *            used.
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, int mask) {

		/*
		 * PART 2
		 */
		int[][] matrix = constructMatrix(version, mask);
		/*
		 * PART 3
		 */
		addDataInformation(matrix, data, mask);

		return matrix;
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** PART 2 *********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create a matrix (2D array) ready to accept data for a given version and mask
	 * 
	 * @param version
	 *            the version number of QR code (has to be between 1 and 4 included)
	 * @param mask
	 *            the mask id to use to mask the data modules. Has to be between 0
	 *            and 7 included to have a valid matrix. If the mask id is not
	 *            valid, the modules would not be not masked later on, hence the
	 *            QRcode would not be valid
	 * @return the qrcode with the patterns and format information modules
	 *         initialized. The modules where the data should be remain empty.
	 */
	public static int[][] constructMatrix(int version, int mask) {
		int[][] qrcodeMatrix = initializeMatrix( version );
		addFinderPatterns( qrcodeMatrix );
		addAlignmentPatterns( qrcodeMatrix, version );
		addTimingPatterns( qrcodeMatrix );
		addDarkModule( qrcodeMatrix );
		addFormatInformation( qrcodeMatrix, mask );
		return qrcodeMatrix;
	}

	/**
	 * Create an empty 2d array of integers of the size needed for a QR code of the
	 * given version
	 * 
	 * @param version
	 *            the version number of the qr code (has to be between 1 and 4
	 *            included
	 * @return an empty matrix
	 */
	public static int[][] initializeMatrix(int version) {
		// get the matrix size thanks to the QRCodeInfos class
		int matrixSize = QRCodeInfos.getMatrixSize( version );
		// return an empty 2D array -> our matrix
		return new int[ matrixSize ][ matrixSize ];
	}

	/**
	 * Add all finder patterns to the given matrix with a border of White modules.
	 * 
	 * @param matrix
	 *            the 2D array to modify: where to add the patterns
	 */
	public static void addFinderPatterns(int[][] matrix) {
		int matrixSize = matrix.length;
		/* we declare the top-left module's coords of each finder
		*  we consider that the finer patterns are 8*8 squares
		*  cut by the qrcode borders
		* */
		int[][] finders = {
				{ -1, -1 }, // top-left module
				{ matrixSize - 8, -1 }, // top-right module
				{ -1, matrixSize - 8 } // bottom-left module
		};
		// go through each finder
		for ( int[] f : finders )
		{
			int topLeftX = f[ 0 ], topLeftY = f[ 1 ];
			int minBoundX = topLeftX + 2;
			int maxBoundX = topLeftX + 6;
			int minBoundY = topLeftY + 2;
			int maxBoundY = topLeftY + 6;

			// go through each module inside the finder
			for ( int i = topLeftY; i < topLeftY + 9; i++ )
			{
				// create the finder pattern
				for ( int j = topLeftX; j < topLeftX + 9; j++ )
				{
					// if i and j are outside the qrcode ( yes this can happen since we also have negative / outside qrcode i's and j's )
					if ( i < 0 || j < 0 || i >= matrixSize || j >= matrixSize ) { continue; }
					// by default the color is set to black
					int color = BLACK_COLOR;
					/*
					 * the white parts :
					 *  a white outline at the surroundings
					 *  and a inline part
					 */
					if (
						( i == topLeftY )     || // top outline
						( i == topLeftY + 8 ) || // bottom outline
						( j == topLeftX )     || // left outline
						( j == topLeftX + 8 ) || // right outline
						( i == minBoundY && j >= minBoundX && j <= maxBoundX ) || // top inline
						( i == maxBoundY && j >= minBoundX && j <= maxBoundX ) || // bottom inline
						( j == minBoundX && i >= minBoundY && i <= maxBoundY ) || // left inline
						( j == maxBoundX && i >= minBoundY && i <= maxBoundY )    // right inline
					) {
						color = WHITE_COLOR;
					}
					// set the matrix color to the corresponding one
					matrix[ i ][ j ] = color;
				}
			}
		}
	}

	/**
	 * Add the alignment pattern if needed, does nothing for version 1
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 * @param version
	 *            the version number of the QR code needs to be between 1 and 4
	 *            included
	 */
	public static void addAlignmentPatterns(int[][] matrix, int version) {
		// make sure the version is > 1, if it is not, then we just don't need the alignment pattern
		if ( version <= 1 ) return;
		int matrixSize = matrix.length;
		// for version < 5, the alignment pattern top-left corner is located at (matriwWidth - 8, matrixHeight - 8)
		// the alignment pattern is a 5 by 5 square
		for ( int i = 0; i < 5; i++ )
		{
			for ( int j = 0; j < 5; j++ )
			{
				int color = BLACK_COLOR;
				// the coordinates of the white outline modules
				if (
					( i == 1 && j >= 1 && j <= 3 ) ||
					( i == 3 && j >= 1 && j <= 3 ) ||
					( j == 1 && i >= 1 && i <= 3 ) ||
					( j == 3 && i >= 1 && i <= 3 )
				) {
					color = WHITE_COLOR;
				}
				matrix[ matrixSize - 9 + i ][ matrixSize - 9 + j ] = color;
			}
		}
	}

	/**
	 * Add the timings patterns
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 */
	public static void addTimingPatterns(int[][] matrix) {
		int padding = 8; // finder patterns are 8x8
		// we draw the two timing pattern lines at the same time !
		for ( int i = padding; i < matrix.length - padding; i++ )
		{
			// we alternate between black and white ->  0 - 1 - 0 - 1 ...
			int color = ( i % 2 == 0 ) ? BLACK_COLOR : WHITE_COLOR;
			matrix[ 6 ][ i ] = color;
			matrix[ i ][ 6 ] = color;
		}
	}

	/**
	 * Add the dark module to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code
	 */
	public static void addDarkModule(int[][] matrix) {
		matrix[ 8 ][ matrix.length - 8 ] = BLACK_COLOR;
	}

	/**
	 * Add the format information to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code to modify
	 * @param mask
	 *            the mask id
	 */
	public static void addFormatInformation(int[][] matrix, int mask) {
		int matrixSize = matrix.length;
		boolean[] formatSequence = QRCodeInfos.getFormatSequence( mask );
		// we don't draw the format information in the middle of the qrcode
		// -> from 8 to matrixSize - 8
		int[] avoidPath = { 8, matrixSize - 8 };
		// we draw the format information two times,
		// Instead of doing it the way it is supposed to be done,
		// we do it in an easier way : the format informations can
		// be viewed as one row and one column, each of them
		// separated by the qrcode data itself
		int row = 8;
		int col = 0;
		for ( int i = 0; i < formatSequence.length; i++ )
		{
			// we must also avoid the timing pattern ! (depending on the version i think..)
			if ( col == 6 || ( col >= avoidPath[ 0 ] && col < avoidPath[ 1 ] ) ) { i--; col++; continue; }
			matrix[ col ][ row ] = formatSequence[ i ] ? BLACK_COLOR : WHITE_COLOR;
			col++;
		}

		col = 8;
		row = matrixSize-1;
		for ( int i = 0; i < formatSequence.length; i++ )
		{
			if ( row == 6 || ( row > avoidPath[ 0 ] && row < avoidPath[ 1 ] + 1 ) ) { i--; row--; continue; }
			matrix[ col ][ row ] = formatSequence[ i ] ? BLACK_COLOR : WHITE_COLOR;
			row--;
		}
	}

	/*
	 * =======================================================================
	 * ****************************** PART 3 *********************************
	 * =======================================================================
	 */

	/**
	 * Choose the color to use with the given coordinate using the masking 0
	 * 
	 * @param col
	 *            x-coordinate
	 * @param row
	 *            y-coordinate
	 * @param dataBit
	 *            : bit value we use to determine the color
	 * @param masking
	 * 			  : mask pattern number
	 * @return the color with the masking
	 */
	public static int maskColor(int col, int row, boolean dataBit, int masking) {
		// if masking is not a valid mask number
		if ( masking < 0 || masking > 7 )
		{
			return dataBit ? BLACK_COLOR : WHITE_COLOR;
		}
		boolean mask = false;
		// use the mask formulas depending on the masking value
		switch ( masking )
		{
			case 0:
				mask = ( col + row ) % 2 == 0;
				break;
			case 1:
				mask = row % 2 == 0;
				break;
			case 2:
				mask = col % 3 == 0;
				break;
			case 3:
				mask = ( col + row ) % 3 == 0;
				break;
			case 4:
				mask = ( Math.floor( row / 2 ) + Math.floor( col / 3 ) ) % 2 == 0;
				break;
			case 5:
				mask = ( ( col * row ) % 2 ) + ( ( col * row ) % 3 ) == 0;
				break;
			case 6:
				mask = ( ( ( col * row ) % 2 ) + ( ( col * row ) % 3 ) ) % 2 == 0;
				break;
			case 7:
				mask = ( ( ( col + row ) % 2 ) + ( ( col + row ) % 3 ) ) % 2 == 0;
				break;
		}
		if ( mask ) dataBit =! dataBit;
		// return the correct color
		return dataBit ? BLACK_COLOR : WHITE_COLOR;
	}

	/**
	 * Add the data bits into the QR code matrix
	 * 
	 * @param matrix
	 *            a 2-dimensionnal array where the bits needs to be added
	 * @param data
	 *            the data to add
	 */
	public static void addDataInformation(int[][] matrix, boolean[] data, int mask) {
		int matrixSize = matrix.length;

		// ( dataIndex < data.length ? data[ dataIndex ] : false )

		// the index we are looking for in the data array
		int dataIndex = 0;

		// a pillar is made up of two columns / two rows -> used for the zigzag path
		int nbPillar = ( matrix.length-1 ) / 2;
		// when we iterate through the row, we either go from matrixSize to -1 and dirRow = -1 -> goingUp = true
		// or we go from 0 to matrixSize-1 and dirRow = 1 -> goingUp = false
		int row = 0; int maxRow = 0; int dirRow = 0;

		boolean goingUp = true; // path orientation

		for ( int pillarCol = nbPillar; pillarCol >= 0; pillarCol-- )
		{
			if ( goingUp ) { row = matrixSize-1; maxRow = -1; dirRow = -1; }
			else if ( !goingUp ) { row = 0; maxRow = matrixSize; dirRow = 1; }

			int col = pillarCol * 2;
			// if we encounter the vertical timing pattern we need to shift the columns by -1







			if ( pillarCol <= 3 && pillarCol > 0 ) { col--; }

			// replace the for loop by a while because something the row variable can either increase or decrease
			while ( row != maxRow )
			{
				// if the module has not been drawn, then we can apply the data
				if ( matrix[ col ][ row ] == 0 )
				{
					// draw the matrix at (col; row)
					drawMatrix( matrix, col, row, dataIndex, data, mask );
					dataIndex++;
				}

				// same as previously but with col - 1 and we need to avoid getting away from the qrcode (basically at col = -1)
				if ( col > 0 && matrix[ col-1 ][ row ] == 0 )
				{
					// draw the matrix at (col-1; row)
					drawMatrix( matrix, col-1, row, dataIndex, data, mask );
					dataIndex++;
				}
				// increase or decrease the row depending on whether we're going up or down
				row += dirRow;
			}
			// once we are at the top / bottom of the qrcode, flip the path orientation
			goingUp = !goingUp;
		}
	}


	private static void drawMatrix( int[][] matrix,
									int col, int row,
									int dataIndex, boolean[] d,
									int mask )
	{
		// check if the dataIndex is inferior to the data length
		// if it is not, set the dataBit to false
		boolean dataBit = dataIndex < d.length && d[ dataIndex ];
		// and get the color the module needs to be depending on the dataBit value and the mask
		matrix[ col ][ row ] = maskColor( col, row, dataBit, mask );
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** BONUS **********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * The mask is computed automatically so that it provides the least penalty
	 * 
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data) {

		int mask = findBestMasking(version, data);

		return renderQRCodeMatrix(version, data, mask);
	}

	/**
	 * Find the best mask to apply to a QRcode so that the penalty score is
	 * minimized. Compute the penalty score with evaluate
	 * 
	 * @param data
	 * @return the mask number that minimize the penalty
	 */
	public static int findBestMasking(int version, boolean[] data) {
		// TODO BONUS
		return 0;
	}

	/**
	 * Compute the penalty score of a matrix
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @return the penalty score obtained by the QR code, lower the better
	 */
	public static int evaluate(int[][] matrix) {
		//TODO BONUS
	
		return 0;
	}

}
