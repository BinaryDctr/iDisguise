package de.robingrether.idisguise.disguise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.Player;
import de.robingrether.util.StringUtil;

/**
 * Represents a disguise.
 * 
 * @since 2.1.3
 * @author RobinGrether
 */
public abstract class Disguise implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 3699593353745149494L;
	protected final DisguiseType type;
	private Visibility visibility = Visibility.EVERYONE;
	private final List<String> visibilityParameter = new ArrayList<String>();
	
	protected Disguise(DisguiseType type) {
		if(!type.isAvailable()) {
			throw new OutdatedServerException();
		}
		this.type = type;
	}
	
	/**
	 * Returns the disguise type
	 * 
	 * @since 2.1.3
	 * @return the disguise type
	 */
	public DisguiseType getType() {
		return this.type;
	}
	
	/**
	 * Gets the visibility mode.
	 * 
	 * @since 5.6.1
	 * @return the visibility mode
	 */
	public Visibility getVisibility() {
		return visibility;
	}
	
	/**
	 * Sets the visibility mode.<br>
	 * Calling this function will reset the visibility parameters.
	 * 
	 * @since 5.6.1
	 * @param visibility the visibility mode
	 */
	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
		visibilityParameter.clear();
	}
	
	/**
	 * Gets the visibility parameters.
	 * 
	 * @since 5.6.1
	 * @return the visibility parameters
	 */
	public String[] getVisibilityParameter() {
		return visibilityParameter.toArray(new String[0]);
	}
	
	/**
	 * Sets the visibility parameters.
	 * 
	 * @since 5.6.1
	 * @param visibilityParameter the visibility parameters
	 */
	public void setVisibilityParameter(String... visibilityParameter) {
		this.visibilityParameter.clear();
		for(String parameter : visibilityParameter) {
			this.visibilityParameter.add(parameter.toLowerCase(Locale.ENGLISH));
		}
	}
	
	/**
	 * Indicates whether this disguise is visible to a certain player.
	 * 
	 * @since 5.6.1
	 * @param player the viewer to indicate for
	 * @return <code>true</code> in case this disguise is visible to the given player, <code>false</code> otherwise
	 */
	public boolean isVisibleTo(Player player) {
		switch(visibility) {
			case EVERYONE:
				return true;
			case ONLY_LIST:
				return visibilityParameter.contains(player.getName().toLowerCase(Locale.ENGLISH));
			case NOT_LIST:
				return !visibilityParameter.contains(player.getName().toLowerCase(Locale.ENGLISH));
			case ONLY_PERMISSION:
				return player.hasPermission(visibilityParameter.get(0));
			case NOT_PERMISSION:
				return !player.hasPermission(visibilityParameter.get(0));
			default:
				return false;
		}
	}
	
	/**
	 * Creates and returns a copy of this object.
	 * 
	 * @since 3.0.1
	 * @return a clone of this instance
	 */
	public final Disguise clone() {
		return fromString(toString());
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @since 3.0.1
	 * @return <code>true</code> if this object is the same as the <code>object</code> argument; <code>false</code> otherwise
	 */
	public final boolean equals(Object object) {
		return object != null && object.getClass().equals(getClass()) && toString().equals(object.toString());
	}
	
	/**
	 * Returns a hash code value for this object.<br>
	 * The hash code is obtained by executing <code>disguise.toString().hashCode()</code>
	 * 
	 * @since 5.6.1
	 * @return a hash code value for this instance
	 */
	public final int hashCode() {
		return toString().hashCode();
	}
	
	/**
	 * Returns a string representation of the object.
	 * 
	 * @since 5.3.1
	 * @return a string representation of the object
	 */
	public String toString() {
		return type.toString() + "; visibility=" + visibility.name().toLowerCase(Locale.ENGLISH) + "; visibility-param=" + StringUtil.join(",", visibilityParameter.toArray(new String[0]));
	}
	
	static {
		Subtypes.registerParameterizedSubtype(Disguise.class, "setVisibility", "visibility", Visibility.class);
		Subtypes.registerParameterizedSubtype(Disguise.class, "setVisibilityParameter", "visibility-param", String[].class);
	}
	
	/**
	 * Recreates a disguise from its string representation.
	 * 
	 * @since 5.5.1
	 * @param string the exact string representation of the disguise
	 * @throws IllegalArgumentException in case the given string cannot be evaluated to a valid disguise
	 * @throws OutdatedServerException in case the Minecraft server does not support the disguise type
	 * @return the recreated disguise object
	 */
	public static Disguise fromString(String string) throws IllegalArgumentException, OutdatedServerException {
		String[] args = string.split("; ");
		DisguiseType type = DisguiseType.Matcher.match(args[0]);
		if(type == null) {
			if(StringUtil.equals(args[0], "player") && args.length == 5) {
				Disguise disguise = new PlayerDisguise(args[3], args[4]);
				Subtypes.applySubtype(disguise, args[1]);
				Subtypes.applySubtype(disguise, args[2]);
				return disguise;
			}
		} else {
			Disguise disguise = type.newInstance();
			for(int i = 1; i < args.length; i++) {
				Subtypes.applySubtype(disguise, args[i]);
			}
			return disguise;
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * This enum represents the different visibility modes.
	 * 
	 * @since 5.6.1
	 * @author RobinGrether
	 */
	public enum Visibility {
		
		/**
		 * The disguise is visible to any player (except the disguised player himself).<br>
		 * The visibility parameter is ignored.
		 */
		EVERYONE,
		/**
		 * The disguise is visible to any player whose name is in the list.<br>
		 * The visibility parameter represents this list.
		 */
		ONLY_LIST,
		/**
		 * The disguise is visible to anyone but those players whose names are in the list.<br>
		 * The visibility parameter represents this list.
		 */
		NOT_LIST,
		/**
		 * The disguise is visible to any player who has a certain permission.<br>
		 * The first entry of the visibility parameter is this permission.
		 */
		ONLY_PERMISSION,
		/**
		 * The disguise is visible to any player who does not have a certain permission.<br>
		 * The first entry of the visibility parameter is this permission.
		 */
		NOT_PERMISSION;
		
	}
	
}